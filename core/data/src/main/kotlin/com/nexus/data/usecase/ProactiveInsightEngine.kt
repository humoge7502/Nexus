package com.nexus.data.usecase

import com.nexus.data.repository.ApplicationRepository
import com.nexus.data.repository.StreakRepository
import com.nexus.data.usecase.CareerMemoryManager
import com.nexus.data.usecase.PlacementReadinessCalculator
import com.nexus.database.dao.GoalDao
import com.nexus.domain.model.ProactiveInsight
import com.nexus.domain.model.InsightCategory
import com.nexus.domain.model.InsightPriority
import kotlinx.coroutines.flow.first
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProactiveInsightEngine @Inject constructor(
  private val streakRepository: StreakRepository,
  private val applicationRepository: ApplicationRepository,
  private val goalDao: GoalDao,
  private val careerMemoryManager: CareerMemoryManager,
  private val placementReadinessCalculator: PlacementReadinessCalculator,
) {

  suspend fun generateDailyInsight(): ProactiveInsight? {
    return try {
      val applicationCounts = applicationRepository.getApplicationCounts().first()
      val streakData = streakRepository.currentStreak.first()
      val activeGoals = goalDao.getActive().first()
      val placement = placementReadinessCalculator.calculate()

      val insights = mutableListOf<ScoredInsight>()

      insights += ScoredInsight(
        insight = ProactiveInsight(
          id = "streak_${System.currentTimeMillis()}",
          category = InsightCategory.GROWTH,
          priority = InsightPriority.HIGH,
          title = "🔥 Day ${streakData.currentStreak} Streak!",
          body = when {
            streakData.currentStreak >= 14 -> "Incredible! 2 weeks of consistency. Don't break the chain!"
            streakData.currentStreak >= 7 -> "One week strong! Real habits forming."
            streakData.currentStreak >= 3 -> "3-day streak! Building unstoppable momentum."
            else -> "Start today! Even 15 minutes counts."
          },
          actionText = if (streakData.currentStreak < 3) "Start Now" else null,
          generatedAt = System.currentTimeMillis(),
        ),
        score = if (streakData.currentStreak >= 7) 100f else 75f,
      )

      insights += ScoredInsight(
        insight = ProactiveInsight(
          id = "applications_${System.currentTimeMillis()}",
          category = InsightCategory.APPLICATION,
          priority = if (applicationCounts.total == 0) InsightPriority.CRITICAL else InsightPriority.NORMAL,
          title = if (applicationCounts.total == 0) "📋 No applications this week" else "📊 ${applicationCounts.total} applications",
          body = when {
            applicationCounts.total == 0 -> "Applying is what gets you hired. Start with 2 today!"
            applicationCounts.interviewing > 0 -> "You have ${applicationCounts.interviewing} interviews lined up!"
            else -> "On track — keep applying consistently."
          },
          actionText = if (applicationCounts.total == 0) "Browse Roles" else null,
          generatedAt = System.currentTimeMillis(),
        ),
        score = if (applicationCounts.total == 0) 90f else 60f,
      )

      insights += ScoredInsight(
        insight = ProactiveInsight(
          id = "goals_${System.currentTimeMillis()}",
          category = InsightCategory.GOAL,
          priority = InsightPriority.NORMAL,
          title = if (activeGoals.isEmpty()) "🎯 No goals yet" else "🎯 ${activeGoals.size} goal(s) active",
          body = if (activeGoals.isEmpty()) {
            "Setting goals triples your success rate. Create one now."
          } else {
            val staleGoal = activeGoals.firstOrNull { it.progress < 0.25f }
            if (staleGoal != null) {
              "${staleGoal.title} is only ${(staleGoal.progress * 100).toInt()}% — push it forward!"
            } else {
              "Solid progress across goals. Keep it up."
            }
          },
          actionText = if (activeGoals.isEmpty()) "Create Goal" else null,
          generatedAt = System.currentTimeMillis(),
        ),
        score = 55f,
      )

      insights += ScoredInsight(
        insight = ProactiveInsight(
          id = "readiness_${System.currentTimeMillis()}",
          category = InsightCategory.GROWTH,
          priority = InsightPriority.NORMAL,
          title = "📈 Readiness: ${placement.score}% — ${placement.level}",
          body = placement.actionItems.firstOrNull()?.description ?: "Keep going — daily practice moves the needle.",
          actionText = placement.actionItems.firstOrNull()?.title,
          generatedAt = System.currentTimeMillis(),
        ),
        score = 50f,
      )

      insights += ScoredInsight(
        insight = ProactiveInsight(
          id = "tip_${System.currentTimeMillis()}",
          category = InsightCategory.LEARNING,
          priority = InsightPriority.LOW,
          title = "💡 Career Tip",
          body = careerMemoryManager.buildLongitudinalContext().take(200).ifBlank {
            "Consistency beats intensity. 30 min focused daily > cramming on weekends."
          },
          actionText = null,
          generatedAt = System.currentTimeMillis(),
        ),
        score = 30f,
      )

      insights.sortedByDescending { it.score }.firstOrNull()?.insight
    } catch (e: Exception) {
      Timber.w(e, "Failed to generate proactive insight")
      null
    }
  }

  private data class ScoredInsight(val insight: ProactiveInsight, val score: Float)
}
