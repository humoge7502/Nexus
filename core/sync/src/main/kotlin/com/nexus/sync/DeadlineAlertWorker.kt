package com.nexus.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.nexus.database.dao.InternshipDao
import com.nexus.database.dao.HackathonDao
import com.nexus.database.dao.CertificationDao
import com.nexus.notifications.NotificationHelper
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import timber.log.Timber
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

@HiltWorker
class DeadlineAlertWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val internshipDao: InternshipDao,
    private val certificationDao: CertificationDao,
    private val hackathonDao: HackathonDao,
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val todayEpochDay = LocalDate.now().toEpochDay()
        val threeDaysFromNowEpochDay = todayEpochDay + 3

        try {
            val cutoffMillis = threeDaysFromNowEpochDay * 86_400_000L
            val internships = internshipDao.getInternshipsWithDeadlineBefore(cutoffMillis, threeDaysFromNowEpochDay)
            internships.forEach { entity ->
                val daysLeft = entity.deadlineEpochDay() - todayEpochDay
                val body = when {
                    daysLeft == 1L -> "Deadline TOMORROW for ${entity.companyName}"
                    daysLeft in 2L..3L -> "$daysLeft days left to apply to ${entity.companyName}"
                    else -> return@forEach
                }
                NotificationHelper.showNotification(
                    context = applicationContext,
                    title = "Deadline Approaching: ${entity.title}",
                    body = body,
                    channelId = NotificationHelper.CHANNEL_OPPORTUNITIES,
                )
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to check internship deadlines")
        }

        try {
            val threeDaysFromNowEpochDay = LocalDate.now().plusDays(3).toEpochDay()
            val hackathons = hackathonDao.getAll().first()
            hackathons.forEach { entity ->
                val regEpochDay = if (entity.registrationDeadline > 10_000_000_000L)
                    Instant.ofEpochMilli(entity.registrationDeadline).atZone(ZoneId.systemDefault()).toLocalDate().toEpochDay()
                else entity.registrationDeadline
                if (regEpochDay in (todayEpochDay + 1)..threeDaysFromNowEpochDay) {
                    NotificationHelper.showNotification(
                        context = applicationContext,
                        title = "Hackathon Deadline: ${entity.title}",
                        body = "Register for ${entity.organizer}'s hackathon!",
                        channelId = NotificationHelper.CHANNEL_OPPORTUNITIES,
                    )
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to check hackathon deadlines")
        }

        return Result.success()
    }

    private fun com.nexus.database.entity.InternshipEntity.deadlineEpochDay(): Long {
        return if (deadline > 10_000_000_000L) {
            Instant.ofEpochMilli(deadline).atZone(ZoneId.systemDefault()).toLocalDate().toEpochDay()
        } else {
            deadline
        }
    }
}
