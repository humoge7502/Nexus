package com.nexus.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.nexus.data.notification.SmartNotificationContentBuilder
import com.nexus.data.repository.StreakRepository
import com.nexus.notifications.NotificationHelper
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import com.nexus.common.Constants
import timber.log.Timber

@HiltWorker
class EveningReviewWorker @AssistedInject constructor(
@Assisted context: Context,
@Assisted params: WorkerParameters,
private val streakRepository: StreakRepository,
private val smartNotificationContentBuilder: SmartNotificationContentBuilder,
) : CoroutineWorker(context, params) {
override suspend fun doWork(): Result {
return try {
val content = smartNotificationContentBuilder.buildEveningContent()
NotificationHelper.showNotification(
context = applicationContext,
title = "🌙 Evening Review",
body = content.body,
channelId = NotificationHelper.CHANNEL_AI,
)
Timber.d("Evening review sent: ${content.title}")
Result.success()
} catch (e: Exception) {
Timber.e(e, "Evening review failed")
if (runAttemptCount < Constants.MAX_RETRY_ATTEMPTS) Result.retry() else Result.failure()
}
}

companion object {
fun schedule(workManager: WorkManager) {
val request = PeriodicWorkRequestBuilder<EveningReviewWorker>(1, java.util.concurrent.TimeUnit.DAYS)
.setConstraints(Constraints.Builder().build())
.build()
workManager.enqueueUniquePeriodicWork("evening_review", ExistingPeriodicWorkPolicy.KEEP, request)
}
}
}
