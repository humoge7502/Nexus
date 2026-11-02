package com.nexus.sync

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.nexus.data.repository.HackathonRepository
import com.nexus.data.repository.InternshipRepository
import com.nexus.datastore.UserPreferences
import com.nexus.domain.model.Hackathon
import com.nexus.domain.model.Internship
import com.nexus.domain.usecase.NewMatchCandidate
import com.nexus.domain.usecase.NewMatchKind
import com.nexus.domain.usecase.NewMatchPlanner
import com.nexus.notifications.NotificationHelper
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import timber.log.Timber
import java.time.LocalDate

/**
 * Tells a student when the catalog gains something they can actually apply for.
 *
 * Every sweep compares the feed against the items this device has already
 * announced, keeps only the arrivals that name a skill the student recorded,
 * and notifies about the single best one. Arrivals with no skill overlap are
 * still recorded as announced — they are not invisible, merely not worth a
 * notification — so the next sweep reports what is genuinely new rather than
 * re-counting yesterday.
 */
@HiltWorker
class NewMatchAlertWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val internshipRepository: InternshipRepository,
    private val hackathonRepository: HackathonRepository,
    private val preferences: UserPreferences,
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            if (!preferences.notificationsEnabled.first()) return Result.success()
            val ownedSkills = preferences.userSkills.first()
            if (ownedSkills.isEmpty()) return Result.success()

            val internships = internshipRepository.getAllInternships().first()
            val hackathons = hackathonRepository.getAllHackathons().first()
            val currentKeys = internships.map { it.key() } + hackathons.map { it.key() }

            preferences.retainAnnouncedOpportunityKeys(currentKeys)
            if (preferences.isOpportunityLogSeeded().not()) {
                preferences.markOpportunitiesAnnounced(currentKeys)
                preferences.markOpportunityLogSeeded()
                Timber.d("New-match alert: catalog recorded without notifying")
                return Result.success()
            }

            val announced = preferences.announcedOpportunityKeys()
            val today = LocalDate.now()
            val candidates = buildList {
                internships
                    .filterNot { it.key() in announced }
                    .mapNotNull { it.toCandidate(ownedSkills) }
                    .let(::addAll)
                hackathons
                    .filterNot { it.key() in announced }
                    .mapNotNull { it.toCandidate(ownedSkills) }
                    .let(::addAll)
            }

            val arrivals = currentKeys.filterNot { it in announced }
            preferences.markOpportunitiesAnnounced(arrivals)

            val pick = NewMatchPlanner.select(candidates, today)
            if (pick != null) {
                notify(pick, candidates.size - 1)
            }
            Result.success()
        } catch (e: Exception) {
            Timber.e(e, "New-match alert sweep failed")
            Result.failure()
        }
    }

    private fun Internship.key(): String = "${NewMatchKind.INTERNSHIP.keyPrefix}:$id"

    private fun Hackathon.key(): String = "${NewMatchKind.HACKATHON.keyPrefix}:$id"

    /**
     * A listing with no skills on it is not a mismatch — it is unrankable, and
     * reporting zero for it would tell the student they failed a bar nobody set.
     */
    private fun Internship.toCandidate(ownedSkills: Set<String>): NewMatchCandidate? {
        val required = requiredSkills.map { it.name }
        if (required.isEmpty()) return null
        val matched = NewMatchPlanner.matchedSkills(required, ownedSkills)
        if (matched.isEmpty()) return null
        return NewMatchCandidate(
            kind = NewMatchKind.INTERNSHIP,
            id = id,
            title = title,
            org = company.name,
            matchedSkills = matched,
            deadline = deadline,
        )
    }

    private fun Hackathon.toCandidate(ownedSkills: Set<String>): NewMatchCandidate? {
        val required = skills.map { it.name }
        if (required.isEmpty()) return null
        val matched = NewMatchPlanner.matchedSkills(required, ownedSkills)
        if (matched.isEmpty()) return null
        return NewMatchCandidate(
            kind = NewMatchKind.HACKATHON,
            id = id,
            title = title,
            org = organizer,
            matchedSkills = matched,
            deadline = registrationDeadline,
        )
    }

    private fun notify(item: NewMatchCandidate, otherCount: Int) {
        val skills = item.matchedSkills.take(2).joinToString(", ")
        val body = buildString {
            append(applicationContext.getString(R.string.new_match_body, item.org, item.title, skills))
            if (otherCount > 0) {
                append(' ')
                append(applicationContext.getString(R.string.new_match_more_body, otherCount))
            }
        }

        NotificationHelper.showNotification(
            context = applicationContext,
            title = applicationContext.getString(R.string.new_match_title, item.org),
            body = body,
            channelId = NotificationHelper.CHANNEL_OPPORTUNITIES,
            pendingIntent = deepLink(item),
        )
    }

    private fun deepLink(item: NewMatchCandidate): PendingIntent? = runCatching {
        val intent = Intent().apply {
            setClassName(applicationContext.packageName, MAIN_ACTIVITY)
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(
                "notification_type",
                if (item.kind == NewMatchKind.HACKATHON) "hackathon" else "opportunity",
            )
            putExtra("target_id", item.id)
        }
        PendingIntent.getActivity(
            applicationContext, item.key().hashCode(), intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }.getOrNull()

    companion object {
        const val WORK_NAME = "new_match_alert"
        private const val MAIN_ACTIVITY = "com.nexus.app.MainActivity"
    }
}
