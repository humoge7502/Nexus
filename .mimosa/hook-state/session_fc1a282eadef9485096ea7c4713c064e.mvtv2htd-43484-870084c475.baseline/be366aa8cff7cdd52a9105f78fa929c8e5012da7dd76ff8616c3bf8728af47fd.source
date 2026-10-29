package com.nexus.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.nexus.data.usecase.ProactiveInsightEngine
import com.nexus.datastore.UserPreferences
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import timber.log.Timber

@HiltWorker
class ProactiveInsightWorker @AssistedInject constructor(
  @Assisted private val context: Context,
  @Assisted params: WorkerParameters,
  private val insightEngine: ProactiveInsightEngine,
  private val userPreferences: UserPreferences,
) : CoroutineWorker(context, params) {

  override suspend fun doWork(): Result {
    return try {
      val insight = insightEngine.generateDailyInsight()
      if (insight != null) {
        userPreferences.saveLatestInsight(
          title = insight.title,
          body = insight.body,
          category = insight.category.name,
          actionText = insight.actionText,
        )
        Timber.i("Daily insight saved: ${insight.title}")
      }
      Result.success()
    } catch (e: Exception) {
      Timber.e(e, "ProactiveInsightWorker failed")
      Result.retry()
    }
  }

  companion object {
    const val WORK_NAME = "proactive_insight_work"
  }
}