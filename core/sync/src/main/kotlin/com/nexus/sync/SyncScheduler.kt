package com.nexus.sync

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.nexus.data.usecase.CareerMemoryManager
import com.nexus.sync.DeadlineNotificationWorker
import com.nexus.sync.ProactiveInsightWorker
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Calendar
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncScheduler @Inject constructor(
  @ApplicationContext private val context: Context,
  private val careerMemoryManager: CareerMemoryManager,
) {
  private val workManager = WorkManager.getInstance(context)

  fun scheduleAll() {
    scheduleHighPriority()
    scheduleLowPriority()
    scheduleMaintenance()
  }

  fun scheduleHighPriority() {
    schedule<SyncWorker>(
      "opportunity_sync", 6, TimeUnit.HOURS,
      Constraints.Builder()
        .setRequiredNetworkType(NetworkType.UNMETERED)
        .setRequiresBatteryNotLow(true)
        .build(),
    )
    schedule<FirestoreSyncWorker>(
      "firestore_sync", 6, TimeUnit.HOURS,
      Constraints.Builder()
        .setRequiredNetworkType(NetworkType.UNMETERED)
        .setRequiresBatteryNotLow(true)
        .setRequiresStorageNotLow(true)
        .build(),
    )
  }

  fun scheduleLowPriority() {
    schedule<LeetCodeSyncWorker>(
      "leetcode_sync", 12, TimeUnit.HOURS,
      Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .setRequiresBatteryNotLow(true)
        .build(),
    )
    schedule<StreakWorker>(
      "streak_check", 24, TimeUnit.HOURS,
      Constraints.Builder()
        .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
        .build(),
    )
    schedule<DeadlineAlertWorker>(
      "deadline_alert", 6, TimeUnit.HOURS,
      Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build(),
    )
    scheduleDailyBriefing()
    scheduleDeadlineNotifications()
    scheduleProactiveInsight()
  }

  fun scheduleMaintenance() {
    scheduleCareerMemoryMaintenance()
  }

  private fun scheduleProactiveInsight() {
    val now = Calendar.getInstance()
    val target = Calendar.getInstance().apply {
      set(Calendar.HOUR_OF_DAY, 8)
      set(Calendar.MINUTE, 0)
      set(Calendar.SECOND, 0)
      if (before(now)) add(Calendar.DAY_OF_MONTH, 1)
    }
    val delay = target.timeInMillis - now.timeInMillis
    val request = PeriodicWorkRequestBuilder<ProactiveInsightWorker>(24, TimeUnit.HOURS)
      .setInitialDelay(delay, TimeUnit.MILLISECONDS)
      .setConstraints(
        Constraints.Builder()
          .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
          .setRequiresBatteryNotLow(true)
          .build(),
      )
      .build()
    workManager.enqueueUniquePeriodicWork(
      ProactiveInsightWorker.WORK_NAME,
      ExistingPeriodicWorkPolicy.KEEP,
      request,
    )
  }

    private fun scheduleDailyBriefing() {
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 7)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            if (before(now)) add(Calendar.DAY_OF_MONTH, 1)
        }
        val delay = target.timeInMillis - now.timeInMillis

        val request = PeriodicWorkRequestBuilder<DailyBriefingWorker>(
            24, TimeUnit.HOURS
        )
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .setRequiresBatteryNotLow(true)
                    .build()
            )
            .build()

        workManager.enqueueUniquePeriodicWork(
            "daily_briefing",
            ExistingPeriodicWorkPolicy.KEEP,
            request,
        )
    }

    private fun scheduleCareerMemoryMaintenance() {
val request = PeriodicWorkRequestBuilder<CareerMemoryMaintenanceWorker>(7, TimeUnit.DAYS)
.setConstraints(
Constraints.Builder()
.setRequiresDeviceIdle(true)
.setRequiresBatteryNotLow(true)
.build()
)
.build()
workManager.enqueueUniquePeriodicWork(
"career_memory_maintenance",
ExistingPeriodicWorkPolicy.KEEP,
request,
)
}

private fun scheduleDeadlineNotifications() {
val now = Calendar.getInstance()
val target = Calendar.getInstance().apply {
set(Calendar.HOUR_OF_DAY, 10)
set(Calendar.MINUTE, 0)
set(Calendar.SECOND, 0)
if (before(now)) add(Calendar.DAY_OF_MONTH, 1)
}
val initialDelay = target.timeInMillis - now.timeInMillis
val request = PeriodicWorkRequestBuilder<DeadlineNotificationWorker>(24, TimeUnit.HOURS)
.setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
.setConstraints(
Constraints.Builder()
.setRequiredNetworkType(NetworkType.NOT_REQUIRED)
.setRequiresBatteryNotLow(true)
.build()
)
.build()
workManager.enqueueUniquePeriodicWork(
DeadlineNotificationWorker.WORK_NAME,
ExistingPeriodicWorkPolicy.KEEP,
request,
)
}

private inline fun <reified W : CoroutineWorker> schedule(
        workName: String,
        interval: Long,
        unit: TimeUnit,
        constraints: Constraints,
    ) {
        val request = PeriodicWorkRequestBuilder<W>(interval, unit)
            .setConstraints(constraints)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 15, TimeUnit.MINUTES)
            .build()

        workManager.enqueueUniquePeriodicWork(
            workName,
            ExistingPeriodicWorkPolicy.KEEP,
            request,
        )
    }
}
