package com.nexus.sync

import android.content.Context
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Calendar
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val workManager = WorkManager.getInstance(context)

    fun scheduleAll() {
        scheduleHighPriority()
        scheduleLowPriority()
        scheduleMaintenance()
    }

    fun scheduleHighPriority() {
        // The verified feed is a single JSON file a little under 100 KB. Holding
        // it to Wi-Fi would leave a student on mobile data with a stale catalog
        // and no deadline alerts, which is the one failure mode this app cannot
        // have.
        schedule<SyncWorker>(
            "opportunity_sync", 6, TimeUnit.HOURS,
            Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .setRequiresBatteryNotLow(true)
                .build(),
        )
        // Cross-device backup is the one job allowed to wait for Wi-Fi: it
        // uploads the student's own data, and a metered connection should not be
        // billed for that without them choosing it.
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
        // Warms the GitHub/Codeforces/LeetCode caches behind the Profile
        // screen. Handles live in DataStore; with none connected the worker
        // exits immediately, so the schedule is unconditional like the rest.
        schedule<PlatformSyncWorker>(
            PlatformSyncWorker.WORK_NAME, 12, TimeUnit.HOURS,
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
        // Reminders read only local tables, so requiring connectivity would hold
        // a same-day deadline warning hostage to the network.
        schedule<DeadlineAlertWorker>(
            "deadline_alert", 6, TimeUnit.HOURS,
            Constraints.Builder()
                .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                .build(),
        )
        // The catalog is already in Room when the feed sync runs, so matching it
        // needs no network of its own — a student with a new match hears about it
        // on the next sweep whether or not they are online.
        schedule<NewMatchAlertWorker>(
            NewMatchAlertWorker.WORK_NAME, 6, TimeUnit.HOURS,
            Constraints.Builder()
                .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                .build(),
        )
        scheduleDailyBriefing()
        scheduleEveningReview()
        scheduleProactiveInsight()
        schedule<WeeklyRetentionWorker>(
            WeeklyRetentionWorker.WORK_NAME, 7, TimeUnit.DAYS,
            Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build(),
        )
    }

    fun scheduleMaintenance() {
        scheduleCareerMemoryMaintenance()
    }

    private fun scheduleProactiveInsight() {
        val request = PeriodicWorkRequestBuilder<ProactiveInsightWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(delayUntil(8, 0), TimeUnit.MILLISECONDS)
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
        val request = PeriodicWorkRequestBuilder<DailyBriefingWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(delayUntil(7, 0), TimeUnit.MILLISECONDS)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .setRequiresBatteryNotLow(true)
                    .build(),
            )
            .build()
        workManager.enqueueUniquePeriodicWork(
            "daily_briefing",
            ExistingPeriodicWorkPolicy.KEEP,
            request,
        )
    }

    private fun scheduleEveningReview() {
        val request = PeriodicWorkRequestBuilder<EveningReviewWorker>(24, TimeUnit.HOURS)
            .setInitialDelay(delayUntil(21, 0), TimeUnit.MILLISECONDS)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                    .build(),
            )
            .build()
        workManager.enqueueUniquePeriodicWork(
            EveningReviewWorker.WORK_NAME,
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
                    .build(),
            )
            .build()
        workManager.enqueueUniquePeriodicWork(
            "career_memory_maintenance",
            ExistingPeriodicWorkPolicy.KEEP,
            request,
        )
    }

    /** Milliseconds until the next occurrence of a local wall-clock time. */
    private fun delayUntil(hourOfDay: Int, minute: Int): Long {
        val now = Calendar.getInstance()
        val target = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hourOfDay)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (before(now)) add(Calendar.DAY_OF_MONTH, 1)
        }
        return target.timeInMillis - now.timeInMillis
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
