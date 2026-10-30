package com.nexus.growth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexus.analytics.AnalyticsTracker
import com.nexus.common.Constants
import com.nexus.data.repository.AchievementRepository
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
data class GrowthRadarAxis(val label: String, val value: Float)

@Immutable
data class GrowthUiState(
  val problems: UiState<List<DSAProblem>> = UiState.Loading,
  val goals: UiState<List<Goal>> = UiState.Loading,
  val selectedDifficulty: DsaDifficulty? = null,
  val nexusScore: Int = 0,
  val nexusScoreFloat: Float = 0f,
  val dailyChallenge: DSAProblem? = null,
  val currentStreak: Int = 0,
  val longestStreak: Int = 0,
  val dsaSolved: Int = 0,
  val dsaTotal: Int = 0,
  val dsaProgress: Float = 0f,
  val totalXP: Int = 0,
  val level: Int = 1,
  val levelName: String = "Rookie",
  val projectsCount: Int = 0,
  val completedGoals: Int = 0,
  val skillProgress: Float = 0f,
  val opportunityEngagement: Float = 0f,
  val consistency: Float = 0f,
  val portfolioStrength: Float = 0f,
  val achievementsProgress: Float = 0f,
  val weekActivity: List<Boolean> = List(7) { false },
  val lastActiveToday: Boolean = false,
  val thisMonthActive: Int = 0,
  val scoreWeeklyChange: Int = 0,
  val scoreMomentum: List<Float> = emptyList(),
  val skillAxes: List<GrowthRadarAxis> = emptyList(),
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
    observeSkillAxes()
    observePlacementReadiness()
    observeScoreMomentum()
    analyticsTracker.trackScreenView("growth")
  }

  private fun loadProblems() {
    viewModelScope.launch {
      problemRepository.getAllProblems().collect { problems ->
        val unsolved = problems.filter { it.status != DsaStatus.SOLVED }
        val challenge = unsolved.getOrNull(java.time.LocalDate.now().dayOfYear % unsolved.size.coerceAtLeast(1))
          ?: problems.firstOrNull()
        _uiState.update { it.copy(problems = UiState.Success(problems), dailyChallenge = challenge) }
      }
    }
  }

  private fun observeSkillAxes() {
    viewModelScope.launch {
      val skills = runCatching { skillGraphRepository.getUserSkills() }.getOrDefault(emptyList())
      val axes = skills.groupBy { it.category }
        .map { (category, group) ->
          GrowthRadarAxis(
            label = when (category) {
              com.nexus.domain.model.SkillCategory.MOBILE_DEVELOPMENT -> "Mobile"
              com.nexus.domain.model.SkillCategory.WEB_DEVELOPMENT -> "Web"
              com.nexus.domain.model.SkillCategory.DATA_SCIENCE -> "Data"
              com.nexus.domain.model.SkillCategory.AI_ML -> "AI/ML"
              com.nexus.domain.model.SkillCategory.CLOUD_COMPUTING -> "Cloud"
              com.nexus.domain.model.SkillCategory.CYBERSECURITY -> "Security"
              com.nexus.domain.model.SkillCategory.DEVOPS -> "DevOps"
              com.nexus.domain.model.SkillCategory.BLOCKCHAIN -> "Web3"
              com.nexus.domain.model.SkillCategory.UI_UX -> "Design"
              com.nexus.domain.model.SkillCategory.DATABASE -> "Database"
              com.nexus.domain.model.SkillCategory.PROGRAMMING -> "Code"
              com.nexus.domain.model.SkillCategory.GENERAL -> "Other"
            },
            value = (group.map { it.proficiency }.average().toFloat() / 3f).coerceIn(0f, 1f),
          )
        }
        .sortedByDescending { it.value }
        .take(6)
      _uiState.update { it.copy(skillAxes = axes) }
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
        val achievements = try { achievementRepository.getUnlocked().first() } catch (_: Exception) { emptyList<com.nexus.domain.model.Achievement>() }
        val skillProgress = (solved.toFloat() / total.coerceAtLeast(1)).coerceIn(0f, 1f)
        val opportunityEngagement = (completedGoals.coerceAtMost(20) / 20f).coerceIn(0f, 1f)
        val consistency = (streak.currentStreak.coerceAtMost(365) / 365f).coerceIn(0f, 1f)
        val portfolioStrength = (projectCount.coerceAtMost(10) / 10f).coerceIn(0f, 1f)
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
        val lastActive = runCatching { java.time.LocalDate.parse(streak.lastActivityDate) }.getOrNull()
        val inferredActiveDays = if (lastActive == null || streak.currentStreak <= 0) {
          emptySet()
        } else {
          (0 until streak.currentStreak.coerceAtMost(400))
            .map { lastActive.minusDays(it.toLong()) }
            .toSet()
        }
        val weekStart = today.minusDays(6)
        val weekActivity = (0 until 7).map { weekStart.plusDays(it.toLong()) in inferredActiveDays }
        val thisMonthActive = inferredActiveDays.count { !it.isBefore(today.withDayOfMonth(1)) }
        _uiState.value.copy(
          nexusScore = nexusScoreResult.overallScore,
          nexusScoreFloat = nexusScoreResult.overallScore.toFloat() / 1000f,
          currentStreak = streak.currentStreak,
          longestStreak = streak.longestStreak,
          dsaSolved = solved,
          dsaTotal = total,
          dsaProgress = if (total > 0) solved.toFloat() / total else 0f,
          totalXP = xp,
          level = level,
          levelName = xpRepository.levelName(level),
          projectsCount = projectCount,
          completedGoals = completedGoals,
          skillProgress = skillProgress,
          opportunityEngagement = opportunityEngagement,
          consistency = consistency,
          portfolioStrength = portfolioStrength,
          achievementsProgress = achievementsProgress,
          weekActivity = weekActivity,
          lastActiveToday = lastActive == today,
          thisMonthActive = thisMonthActive,
        )
      }.catch { Timber.e(it, "Growth metrics observation failed") }
       .collect { state ->
         _uiState.value = state
         xpRepository.recordScoreSnapshot(state.nexusScore)
       }
    }
  }

  /** Real daily score history; never synthesised when snapshots are missing. */
  private fun observeScoreMomentum() {
    viewModelScope.launch {
      xpRepository.getSnapshotsForUser()
        .catch { Timber.e(it, "Score momentum observation failed") }
        .collect { snapshots ->
          val scores = snapshots.map { it.score.toFloat() }
          val weekAgo = System.currentTimeMillis() - 7L * 24 * 60 * 60 * 1000
          val baseline = snapshots.lastOrNull { it.recordedAt <= weekAgo }?.score
            ?: snapshots.firstOrNull()?.score
            ?: snapshots.lastOrNull()?.score
            ?: 0
          val weekly = (snapshots.lastOrNull()?.score ?: 0) - baseline
          _uiState.update {
            it.copy(scoreMomentum = scores, scoreWeeklyChange = weekly)
          }
        }
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
