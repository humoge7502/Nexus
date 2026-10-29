package com.nexus.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.nexus.data.repository.StreakRepository
import com.nexus.data.usecase.RetentionTracker
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import timber.log.Timber

@HiltWorker
class WeeklyRetentionWorker @AssistedInject constructor(
  @Assisted private val context: Context,
  @Assisted params: WorkerParameters,
  private val retentionTracker: RetentionTracker,
  private val streakRepository: StreakRepository,
) : CoroutineWorker(context, params) {

  override suspend fun doWork(): Result {
    return try {
      val sessionCount = streakRepository.currentStreak.first().currentStreak
      val featuresUsed = listOf("home", "explore", "ai", "growth")
      retentionTracker.recordWeeklyActivity(sessionCount, featuresUsed)
      Timber.d("WeeklyRetentionWorker complete")
      Result.success()
    } catch (e: Exception) {
      Timber.e(e, "WeeklyRetentionWorker failed")
      if (runAttemptCount < 2) Result.retry() else Result.failure()
    }
  }

  companion object {
    const val WORK_NAME = "weekly_retention"
  }
}