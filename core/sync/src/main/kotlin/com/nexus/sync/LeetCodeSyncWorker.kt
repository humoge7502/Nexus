package com.nexus.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.nexus.data.repository.LeetCodeRepository
import com.nexus.datastore.UserPreferences
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import com.nexus.common.Constants
import kotlinx.coroutines.flow.first
import timber.log.Timber
import java.util.concurrent.TimeUnit

@HiltWorker
class LeetCodeSyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val leetCodeRepository: LeetCodeRepository,
    private val userPreferences: UserPreferences,
) : CoroutineWorker(context, params) {
override suspend fun doWork(): Result {
  val username = userPreferences.leetcodeUsername.first()
  if (username.isBlank()) {
    Timber.d("LeetCode username not set; skipping sync")
    return Result.success()
  }
  return try {
    val statsResult = leetCodeRepository.getStats(username)
    val problemsResult = leetCodeRepository.syncSolvedProblems(username)
    Timber.d("LeetCode sync: stats=${statsResult.isSuccess}, problems=${problemsResult.isSuccess}")
    if (statsResult.isSuccess || problemsResult.isSuccess) Result.success()
    else if (runAttemptCount < Constants.MAX_RETRY_ATTEMPTS) Result.retry()
    else Result.failure()
  } catch (e: Exception) {
    Timber.e(e, "LeetCode sync failed")
    if (runAttemptCount < Constants.MAX_RETRY_ATTEMPTS) Result.retry() else Result.failure()
  }
}

    companion object {
        fun schedule(workManager: WorkManager) {
            val request = PeriodicWorkRequestBuilder<LeetCodeSyncWorker>(6, TimeUnit.HOURS)
                .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
                .build()
            workManager.enqueueUniquePeriodicWork("leetcode_sync", ExistingPeriodicWorkPolicy.KEEP, request)
        }
    }
}
