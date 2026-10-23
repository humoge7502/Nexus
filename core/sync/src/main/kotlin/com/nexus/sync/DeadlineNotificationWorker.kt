package com.nexus.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.nexus.data.repository.ApplicationRepository
import com.nexus.domain.model.ApplicationStatus
import com.nexus.data.repository.InternshipRepository
import com.nexus.data.repository.HackathonRepository
import com.nexus.data.notification.SmartNotificationContentBuilder
import com.nexus.notifications.NotificationHelper
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import timber.log.Timber
import java.time.LocalDate
import java.time.temporal.ChronoUnit

@HiltWorker
class DeadlineNotificationWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val applicationRepository: ApplicationRepository,
    private val internshipRepository: InternshipRepository,
    private val hackathonRepository: HackathonRepository,
    private val contentBuilder: SmartNotificationContentBuilder,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val saved = applicationRepository.getByStatus(ApplicationStatus.SAVED).first()
            val applied = applicationRepository.getByStatus(ApplicationStatus.APPLIED).first()
            val allApps = saved + applied

            val internships = internshipRepository.getAllInternships().first()
            val hackathons = hackathonRepository.getAllHackathons().first()

            val upcoming = allApps.mapNotNull { app ->
                val deadline = if (app.opportunityType == "INTERNSHIP") {
                    internships.firstOrNull { it.id == app.opportunityId }?.deadline
                } else if (app.opportunityType == "HACKATHON") {
                    hackathons.firstOrNull { it.id == app.opportunityId }?.registrationDeadline
                } else null

                if (deadline == null) return@mapNotNull null
                val daysLeft = ChronoUnit.DAYS.between(LocalDate.now(), deadline).toInt()
                if (daysLeft in 1..7) Triple(app.company, app.role, daysLeft) else null
            }.sortedBy { it.third }

            upcoming.firstOrNull()?.let { (company, role, daysLeft) ->
                val content = contentBuilder.buildDeadlineContent(company, role, daysLeft)
                NotificationHelper.showNotification(
                    context = applicationContext,
                    title = content.title,
                    body = content.body,
                    channelId = NotificationHelper.CHANNEL_DEADLINES,
                )
                Timber.d("Deadline notification shown: $company in $daysLeft days")
            }
            Result.success()
        } catch (e: Exception) {
            Timber.e(e, "DeadlineNotificationWorker failed")
            Result.retry()
        }
    }

    companion object {
        const val WORK_NAME = "deadline_notifications"
    }
}
