package com.nexus.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.nexus.common.Constants
import com.nexus.data.repository.StreakRepository
import com.nexus.data.repository.UserXpRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import timber.log.Timber

@HiltWorker
class StreakWorker @AssistedInject constructor(
  @Assisted context: Context,
  @Assisted workerParams: WorkerParameters,
  private val streakRepository: StreakRepository,
  private val xpRepository: UserXpRepository,
) : CoroutineWorker(context, workerParams) {

  override suspend fun doWork(): Result {
    return try {
      streakRepository.checkAndUpdateStreaks()
      val streakData = streakRepository.currentStreak.first()
      Timber.d("Streak check completed: current=${streakData.currentStreak}")
      Result.success()
    } catch (e: Exception) {
      Timber.e(e, "Streak check failed")
      if (runAttemptCount < Constants.MAX_RETRY_ATTEMPTS) Result.retry() else Result.failure()
    }
  }
}
