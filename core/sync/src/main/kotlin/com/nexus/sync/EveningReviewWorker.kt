package com.nexus.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.nexus.common.Constants
import com.nexus.data.notification.SmartNotificationContentBuilder
import com.nexus.datastore.UserPreferences
import com.nexus.notifications.NotificationHelper
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import timber.log.Timber

/**
 * The nightly "did you close today?" nudge. The copy is generated from the real
 * streak and the real XP earned today, and the streak-at-risk warning is the
 * whole point — so it is the title, not something swallowed by a fixed header.
 */
@HiltWorker
class EveningReviewWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val contentBuilder: SmartNotificationContentBuilder,
    private val preferences: UserPreferences,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            if (!preferences.notificationsEnabled.first()) return Result.success()
            val content = contentBuilder.buildEveningContent()
            NotificationHelper.showNotification(
                context = applicationContext,
                title = content.title,
                body = content.body,
                channelId = NotificationHelper.CHANNEL_STREAK,
            )
            Timber.d("Evening review sent: ${content.title}")
            Result.success()
        } catch (e: Exception) {
            Timber.e(e, "Evening review failed")
            if (runAttemptCount < Constants.MAX_RETRY_ATTEMPTS) Result.retry() else Result.failure()
        }
    }

    companion object {
        const val WORK_NAME = "evening_review"
    }
}
