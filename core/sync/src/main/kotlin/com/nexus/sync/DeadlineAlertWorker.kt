package com.nexus.sync

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.nexus.data.repository.ApplicationRepository
import com.nexus.data.repository.toDeadlineDate
import com.nexus.database.dao.HackathonDao
import com.nexus.database.dao.InternshipDao
import com.nexus.database.entity.HackathonEntity
import com.nexus.database.entity.InternshipEntity
import com.nexus.datastore.UserPreferences
import com.nexus.domain.model.ApplicationStatus
import com.nexus.domain.usecase.DeadlineReminderPlanner
import com.nexus.domain.usecase.ReminderTier
import com.nexus.domain.usecase.WatchedDeadline
import com.nexus.notifications.NotificationHelper
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import timber.log.Timber
import java.time.LocalDate
import java.time.LocalTime

/**
 * The deadline engine behind the product promise: a student who saved or tracked
 * an opportunity hears about it before it closes — a week out, three days out,
 * the day before, the morning of, and the final hours.
 *
 * Only saved and pipeline items are watched, and each tier fires once per
 * opportunity for the life of that deadline, so the six-hourly sweep cannot turn
 * into a repeating ping. At most one notification leaves a sweep: the most urgent
 * item, plus a count of what else is close.
 */
@HiltWorker
class DeadlineAlertWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val internshipDao: InternshipDao,
    private val hackathonDao: HackathonDao,
    private val applicationRepository: ApplicationRepository,
    private val preferences: UserPreferences,
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            if (!preferences.notificationsEnabled.first()) return Result.success()

            val today = LocalDate.now()
            val watched = buildWatchList(today)

            // Keys for items no longer saved, no longer in the pipeline, or already
            // past their deadline would otherwise accumulate forever.
            preferences.retainDeadlineReminderKeys(watched.map { "${it.kind}:${it.id}" })
            if (watched.isEmpty()) return Result.success()

            val fired = preferences.firedDeadlineReminderKeys()
            val hourOfDay = LocalTime.now().hour
            val decisions = watched.mapNotNull { item ->
                DeadlineReminderPlanner.decide(item, today, hourOfDay, fired)?.let { item to it }
            }.sortedBy { (item, _) -> DeadlineReminderPlanner.daysLeft(item, today) }

            val urgent = decisions.firstOrNull() ?: return Result.success()
            val daysLeft = DeadlineReminderPlanner.daysLeft(urgent.first, today)
            preferences.markDeadlineReminderKeysFired(listOf(urgent.second.key))
            notify(urgent.first, urgent.second.tier, daysLeft, decisions.size - 1)
            Result.success()
        } catch (e: Exception) {
            Timber.e(e, "Deadline reminder sweep failed")
            Result.failure()
        }
    }

    private suspend fun buildWatchList(today: LocalDate): List<WatchedDeadline> {
        val pipeline = pipelineOpportunityIds()
        val watched = buildList {
            internshipDao.getAllInternships().first().forEach { entity ->
                val item = entity.toWatchedDeadline() ?: return@forEach
                if (entity.isSaved || entity.id in pipeline) add(item)
            }
            hackathonDao.getAll().first().forEach { entity ->
                val item = entity.toWatchedDeadline() ?: return@forEach
                if (entity.isSaved || entity.id in pipeline) add(item)
            }
        }
        return watched.filter { it.deadline >= today }
    }

    private suspend fun pipelineOpportunityIds(): Set<String> =
        listOf(
            ApplicationStatus.SAVED,
            ApplicationStatus.APPLIED,
            ApplicationStatus.IN_REVIEW,
            ApplicationStatus.INTERVIEW,
            ApplicationStatus.INTERVIEWING,
        ).flatMap { status -> applicationRepository.getByStatus(status).first() }
            .map { it.opportunityId }
            .toSet()

    private fun notify(item: WatchedDeadline, tier: ReminderTier, daysLeft: Long, otherCount: Int) {
        val (titleRes, bodyRes) = when (tier) {
            ReminderTier.WEEK -> R.string.reminder_week_title to R.string.reminder_week_body
            ReminderTier.THREE_DAYS -> R.string.reminder_three_title to R.string.reminder_three_body
            ReminderTier.TOMORROW -> R.string.reminder_tomorrow_title to R.string.reminder_tomorrow_body
            ReminderTier.TODAY -> R.string.reminder_today_title to R.string.reminder_today_body
            ReminderTier.FINAL_HOURS -> R.string.reminder_final_title to R.string.reminder_final_body
        }

        val body = buildString {
            append(applicationContext.getString(bodyRes, item.org, daysLeft))
            if (otherCount > 0) {
                append(' ')
                append(applicationContext.getString(R.string.reminder_more_body, otherCount))
            }
        }

        NotificationHelper.showNotification(
            context = applicationContext,
            title = applicationContext.getString(titleRes, item.title, daysLeft),
            body = body,
            channelId = NotificationHelper.CHANNEL_DEADLINES,
            pendingIntent = deepLink(item),
        )
    }

    private fun deepLink(item: WatchedDeadline): PendingIntent? = runCatching {
        val intent = Intent().apply {
            setClassName(applicationContext.packageName, MAIN_ACTIVITY)
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(
                "notification_type",
                if (item.kind == HACKATHON_KIND) "hackathon" else "deadline",
            )
            putExtra("target_id", item.id)
        }
        PendingIntent.getActivity(
            applicationContext, item.id.hashCode(), intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }.getOrNull()

    private fun InternshipEntity.toWatchedDeadline(): WatchedDeadline? =
        if (deadline <= 0L) null else WatchedDeadline(
            kind = INTERNSHIP_KIND,
            id = id,
            title = title,
            org = companyName,
            deadline = deadline.toDeadlineDate(),
        )

    private fun HackathonEntity.toWatchedDeadline(): WatchedDeadline? =
        if (registrationDeadline <= 0L) null else WatchedDeadline(
            kind = HACKATHON_KIND,
            id = id,
            title = title,
            org = organizer,
            deadline = registrationDeadline.toDeadlineDate(),
        )

    private companion object {
        const val MAIN_ACTIVITY = "com.nexus.app.MainActivity"
        const val INTERNSHIP_KIND = "INTERNSHIP"
        const val HACKATHON_KIND = "HACKATHON"
    }
}
