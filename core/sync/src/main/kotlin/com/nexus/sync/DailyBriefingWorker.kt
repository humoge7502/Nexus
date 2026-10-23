package com.nexus.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.nexus.data.notification.SmartNotificationContentBuilder
import com.nexus.data.repository.StreakRepository
import com.nexus.data.repository.InternshipRepository
import com.nexus.database.dao.GoalDao
import com.nexus.database.dao.DSAProblemDao
import com.nexus.domain.usecase.CareerScoreCalculator
import com.nexus.notifications.NotificationHelper
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import com.nexus.common.Constants
import kotlinx.coroutines.flow.first
import timber.log.Timber

@HiltWorker
class DailyBriefingWorker @AssistedInject constructor(
@Assisted context: Context,
@Assisted params: WorkerParameters,
private val streakRepository: StreakRepository,
private val smartNotificationContentBuilder: SmartNotificationContentBuilder,
) : CoroutineWorker(context, params) {
override suspend fun doWork(): Result {
return try {
val content = smartNotificationContentBuilder.buildMorningContent()
NotificationHelper.showNotification(
context = applicationContext,
title = "☀️ NEXUS Daily Briefing",
body = content.body,
channelId = NotificationHelper.CHANNEL_AI,
)
Timber.d("Daily briefing sent: ${content.title}")
Result.success()
} catch (e: Exception) {
Timber.e(e, "Daily briefing failed")
if (runAttemptCount < Constants.MAX_RETRY_ATTEMPTS) Result.retry() else Result.failure()
}
}

companion object {
fun schedule(workManager: WorkManager) {
val request = PeriodicWorkRequestBuilder<DailyBriefingWorker>(1, java.util.concurrent.TimeUnit.DAYS)
.setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
.build()
workManager.enqueueUniquePeriodicWork("daily_briefing", ExistingPeriodicWorkPolicy.KEEP, request)
}
}
}
