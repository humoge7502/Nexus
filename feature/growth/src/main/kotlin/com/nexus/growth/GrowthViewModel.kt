package com.nexus.growth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexus.analytics.AnalyticsTracker
import com.nexus.common.Constants
import com.nexus.data.repository.AchievementRepository
import com.nexus.data.repository.CertificationRepository
import com.nexus.data.repository.DSAProblemRepository
import com.nexus.data.repository.GoalRepository
import com.nexus.data.repository.PortfolioRepository
import com.nexus.data.repository.StreakRepository
import com.nexus.data.repository.UserXpRepository
import com.nexus.data.repository.SkillGraphRepository
import com.nexus.data.usecase.PlacementReadinessCalculator
import com.nexus.domain.model.DSAProblem
import com.nexus.domain.model.DsaDifficulty
import com.nexus.domain.model.DsaStatus
import com.nexus.domain.model.Goal
import com.nexus.domain.model.UiState
import com.nexus.domain.usecase.CareerScoreCalculator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import androidx.compose.runtime.Immutable
import javax.inject.Inject

@Immutable
data class GrowthUiState(
  val problems: UiState<List<DSAProblem>> = UiState.Loading,
  val goals: UiState<List<Goal>> = UiState.Loading,
  val selectedDifficulty: DsaDifficulty? = null,
  val nexusScore: Int = 0,
  val nexusScoreFloat: Float = 0f,
  val scorePercentile: Int = 5,
  val dailyChallengeTitle: String = "Two Sum II",
  val currentStreak: Int = 0,
  val longestStreak: Int = 0,
  val dsaSolved: Int = 0,
  val dsaTotal: Int = 0,
  val dsaProgress: Float = 0f,
  val totalXP: Int = 0,
  val level: Int = 1,
  val levelName: String = "Rookie",
  val certificationsCount: Int = 0,
  val projectsCount: Int = 0,
  val skillProgress: Float = 0f,
  val opportunityEngagement: Float = 0f,
  val consistency: Float = 0f,
  val portfolioStrength: Float = 0f,
  val communityContribution: Float = 0f,
  val achievementsProgress: Float = 0f,
  val weekActivity: List<Boolean> = listOf(true, true, true, true, true, false, false),
  val thisMonthActive: Int = 0,
  val scoreWeeklyChange: Int = 0,
  val placementScore: Int = 0,
  val placementLevel: String = "Getting Started",
  val placementMonthEstimate: String = "~9 months from now",
  val placementActionItems: List<PlacementReadinessCalculator.PlacementAction> = emptyList(),
)

@HiltViewModel
class GrowthViewModel @Inject constructor(
  private val problemRepository: DSAProblemRepository,
  private val goalRepository: GoalRepository,
  private val streakRepository: StreakRepository,
  private val xpRepository: UserXpRepository,
  private val analyticsTracker: AnalyticsTracker,
  private val certificationRepository: CertificationRepository,
  private val portfolioRepository: PortfolioRepository,
  private val careerScoreCalculator: CareerScoreCalculator,
  private val achievementRepository: AchievementRepository,
  private val placementCalculator: PlacementReadinessCalculator,
  private val skillGraphRepository: SkillGraphRepository,
) : ViewModel() {

  private val _uiState = MutableStateFlow(GrowthUiState())
  val uiState: StateFlow<GrowthUiState> = _uiState.asStateFlow()

  init {
    loadProblems()
    loadGoals()
    observeMetrics()
    observePlacementReadiness()
    analyticsTracker.trackScreenView("growth")
  }

  private fun loadProblems() {
    viewModelScope.launch {
      problemRepository.getAllProblems().collect { problems ->
        _uiState.update { it.copy(problems = UiState.Success(problems)) }
      }
    }
  }

  private fun loadGoals() {
    viewModelScope.launch {
      goalRepository.getAllGoals().collect { goals ->
        _uiState.update { it.copy(goals = UiState.Success(goals)) }
      }
    }
  }

  private fun observeMetrics() {
    viewModelScope.launch {
      combine(
        combine(
          problemRepository.getSolvedCountFlow(),
          problemRepository.getTotalCount(),
          goalRepository.getCompletedCount(),
        ) { solved, total, completedGoals -> Triple(solved, total, completedGoals) },
        streakRepository.currentStreak,
        xpRepository.totalXP,
        xpRepository.currentLevel,
        portfolioRepository.getProjectCount(),
      ) { problemData, streak, xp, level, projectCount ->
        val solved = problemData.first
        val total = problemData.second
        val completedGoals = problemData.third
        val certCount = try { certificationRepository.getCertificationCount() } catch (_: Exception) { 0 }
        val achievements = try { achievementRepository.getUnlocked().first() } catch (_: Exception) { emptyList<com.nexus.domain.model.Achievement>() }
        val skillProgress = (solved.toFloat() / total.coerceAtLeast(1)).coerceIn(0f, 1f)
        val opportunityEngagement = (completedGoals.coerceAtMost(20) / 20f).coerceIn(0f, 1f)
        val consistency = (streak.currentStreak.coerceAtMost(365) / 365f).coerceIn(0f, 1f)
        val portfolioStrength = (projectCount.coerceAtMost(10) / 10f).coerceIn(0f, 1f)
        val communityContribution = (certCount.coerceAtMost(10) / 10f).coerceIn(0f, 1f)
        val achievementsProgress = (achievements.size.coerceAtMost(20) / 20f).coerceIn(0f, 1f)
        val nexusScoreResult = careerScoreCalculator.calculate(
          skillProgress = skillProgress,
          opportunityEngagement = opportunityEngagement,
          streak = streak.currentStreak,
          solvedProblems = solved,
          achievements = achievements,
          completedGoals = completedGoals,
        )
        val today = java.time.LocalDate.now()
        val dayOfWeek = today.dayOfWeek.value
        val weekActivity = (1..7).map { if (it <= dayOfWeek) true else false }
        val thisMonthActive = streak.currentStreak.coerceAtMost(30)
        val dailyChallengeTitles = listOf(
          "Two Sum II", "Reverse Linked List", "Binary Search",
          "Valid Parentheses", "Merge Two Sorted Lists", "Maximum Subarray", "Climbing Stairs",
        )
        val dailyChallengeTitle = dailyChallengeTitles[today.dayOfYear % dailyChallengeTitles.size]
        _uiState.value.copy(
          dailyChallengeTitle = dailyChallengeTitle,
          nexusScore = nexusScoreResult.overallScore,
          nexusScoreFloat = nexusScoreResult.overallScore.toFloat() / 1000f,
          scorePercentile = when {
            nexusScoreResult.overallScore >= 800 -> 95
            nexusScoreResult.overallScore >= 600 -> 80
            nexusScoreResult.overallScore >= 400 -> 60
            nexusScoreResult.overallScore >= 200 -> 35
            nexusScoreResult.overallScore >= 100 -> 15
            else -> 5
          },
          currentStreak = streak.currentStreak,
          longestStreak = streak.longestStreak,
          dsaSolved = solved,
          dsaTotal = total,
          dsaProgress = if (total > 0) solved.toFloat() / total else 0f,
          totalXP = xp,
          level = level,
          levelName = xpRepository.levelName(level),
          certificationsCount = certCount,
          projectsCount = projectCount,
          skillProgress = skillProgress,
          opportunityEngagement = opportunityEngagement,
          consistency = consistency,
          portfolioStrength = portfolioStrength,
          communityContribution = communityContribution,
          achievementsProgress = achievementsProgress,
          weekActivity = weekActivity,
          thisMonthActive = thisMonthActive,
        )
      }.catch { Timber.e(it, "Growth metrics observation failed") }
       .collect { _uiState.value = it }
    }
  }

  private fun observePlacementReadiness() {
    viewModelScope.launch {
      try {
        val readiness = placementCalculator.calculate()
        _uiState.update {
          it.copy(
            placementScore = readiness.score,
            placementLevel = readiness.level,
            placementMonthEstimate = readiness.estimatedPlacementMonth,
            placementActionItems = readiness.actionItems,
          )
        }
      } catch (e: Exception) {
        Timber.e(e, "Failed to calculate placement readiness")
      }
    }
  }

  fun updateProblemStatus(problemId: String, status: DsaStatus) {
    viewModelScope.launch {
      try {
        problemRepository.updateStatus(problemId, status)
      } catch (e: Exception) { Timber.w(e, "Failed to update problem status") }
    }
  }

  fun toggleSolved(problemId: String) {
    viewModelScope.launch {
      try {
        problemRepository.updateStatus(problemId, DsaStatus.SOLVED)
      } catch (e: Exception) { Timber.w(e, "Failed to toggle problem solved status") }
    }
  }

  fun refresh() {
    loadProblems()
    loadGoals()
  }
}
