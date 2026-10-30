package com.nexus.achievements

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexus.analytics.AnalyticsTracker
import com.nexus.common.Constants
import com.nexus.data.repository.AchievementRepository
import com.nexus.data.repository.ApplicationRepository
import com.nexus.data.repository.CertificationRepository
import com.nexus.data.repository.DSAProblemRepository
import com.nexus.data.repository.GoalRepository
import com.nexus.data.repository.LeaderboardRepository
import com.nexus.data.repository.PortfolioRepository
import com.nexus.data.repository.StreakRepository
import com.nexus.data.repository.UserXpRepository
import com.nexus.domain.model.Achievement
import com.nexus.domain.model.AchievementType
import com.nexus.domain.model.ApplicationStatus
import com.nexus.domain.model.ScoreSnapshot
import com.nexus.domain.model.UiState
import com.nexus.domain.repository.LeaderboardTab
import com.nexus.domain.usecase.BadgeUnlockEvaluator
import com.nexus.domain.usecase.CareerScoreCalculator
import com.nexus.domain.usecase.topPercentOf
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
import javax.inject.Inject

@Immutable
data class AchievementsUiState(
    val allAchievements: UiState<List<Achievement>> = UiState.Loading,
    val unlockedAchievements: UiState<List<Achievement>> = UiState.Loading,
    val totalXP: Int = 0,
    val level: Int = 1,
    val levelName: String = "Rookie",
    val xpToNextLevel: Int = 500,
    val nexusScore: Int = 0,
    val weeklyChange: Int = 0,
    val percentile: Int? = null,
    val unlockedBadgeCount: Int = 0,
    val badgeCount: Int = 0,
    val streakDays: Int = 0,
    val celebration: String? = null,
)

@Suppress("LongParameterList")
@HiltViewModel
class AchievementsViewModel @Inject constructor(
    private val repository: AchievementRepository,
    private val xpRepository: UserXpRepository,
    private val analyticsTracker: AnalyticsTracker,
    private val badgeEvaluator: BadgeUnlockEvaluator,
    private val streakRepository: StreakRepository,
    private val dsaProblemRepository: DSAProblemRepository,
    private val certificationRepository: CertificationRepository,
    private val portfolioRepository: PortfolioRepository,
    private val applicationRepository: ApplicationRepository,
    private val goalRepository: GoalRepository,
    private val careerScoreCalculator: CareerScoreCalculator,
    private val leaderboardRepository: LeaderboardRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AchievementsUiState())
    val uiState: StateFlow<AchievementsUiState> = _uiState.asStateFlow()

    private var snapshotsCache: List<ScoreSnapshot> = emptyList()

    init {
        analyticsTracker.track("screen_view", mapOf("screen" to "achievements"))
        loadAchievements()
        loadUnlocked()
        observeXP()
        observeScoreHistory()
        loadPercentile()
        evaluateBadges()
    }

    private fun observeXP() {
        viewModelScope.launch {
            combine(
                xpRepository.totalXP,
                xpRepository.currentLevel,
                xpRepository.xpToNextLevel,
            ) { xp, level, xpToNext ->
                _uiState.value.copy(
                    totalXP = xp,
                    level = level,
                    levelName = xpRepository.levelName(level),
                    xpToNextLevel = xpToNext,
                )
            }.collect { _uiState.value = it }
        }
    }

    private fun loadAchievements() {
        viewModelScope.launch {
            repository.getAll()
                .map<List<Achievement>, UiState<List<Achievement>>> { UiState.Success(it) }
                .catch { e -> emit(UiState.Error(e.message ?: "Failed to load achievements")) }
                .collect { _uiState.value = _uiState.value.copy(allAchievements = it) }
        }
        viewModelScope.launch {
            repository.getAll()
                .collect { achievements ->
                    _uiState.update { it.copy(badgeCount = achievements.size) }
                }
        }
    }

    private fun loadUnlocked() {
        viewModelScope.launch {
            repository.getUnlocked()
                .map<List<Achievement>, UiState<List<Achievement>>> { UiState.Success(it) }
                .catch { e -> emit(UiState.Error(e.message ?: "Failed to load unlocked achievements")) }
                .collect { _uiState.value = _uiState.value.copy(unlockedAchievements = it) }
        }
        viewModelScope.launch {
            repository.getUnlocked()
                .collect { unlocked ->
                    _uiState.update { it.copy(unlockedBadgeCount = unlocked.size) }
                }
        }
    }

    private fun evaluateBadges() {
        viewModelScope.launch {
            combine(
                combine(
                    streakRepository.currentStreak,
                    dsaProblemRepository.getSolvedCountFlow(),
                    dsaProblemRepository.getTotalCount(),
                ) { streak, solved, total -> Triple(streak, solved, total) },
                combine(
                    portfolioRepository.getProjectCount(),
                    applicationRepository.getAllApplications(),
                    certificationRepository.getCompletedCertificationCount(),
                ) { projects, applications, certs -> Triple(projects, applications, certs) },
                goalRepository.getCompletedCount(),
                xpRepository.totalXP,
            ) { problems, activity, completedGoals, xp ->
                val (streak, solved, totalProblems) = problems
                val (projects, applications, certs) = activity
                val submittedApplications = applications.count {
                    it.status != ApplicationStatus.SAVED && it.status != ApplicationStatus.NOT_APPLIED
                }
                val unlockedBefore = repository.getUnlocked().first()
                val stats = BadgeUnlockEvaluator.UserStats(
                    streakDays = streak.currentStreak,
                    problemsSolved = solved,
                    certificationsCompleted = certs,
                    applicationsSubmitted = submittedApplications,
                    projectsCompleted = projects,
                    totalXP = xp,
                )
                val newBadges = badgeEvaluator.evaluate(stats, unlockedBefore.map { it.type }.toSet())
                for (badge in newBadges) {
                    repository.unlock(badge.name.lowercase())
                    analyticsTracker.track("badge_unlocked", mapOf("badge" to badge.name))
                    Timber.d("Badge unlocked: ${badge.name}")
                }
                if (newBadges.isNotEmpty()) {
                    _uiState.update { it.copy(celebration = newBadges.first().name) }
                }
                val unlockedAchievements = if (newBadges.isEmpty()) unlockedBefore else {
                    repository.getUnlocked().first()
                }
                val skillProgress =
                    (solved.toFloat() / totalProblems.coerceAtLeast(1)).coerceIn(0f, 1f)
                val opportunityEngagement = (completedGoals.coerceAtMost(20) / 20f).coerceIn(0f, 1f)
                val score = careerScoreCalculator.calculate(
                    skillProgress = skillProgress,
                    opportunityEngagement = opportunityEngagement,
                    streak = streak.currentStreak,
                    solvedProblems = solved,
                    achievements = unlockedAchievements,
                    completedGoals = completedGoals,
                )
                _uiState.update {
                    it.copy(
                        streakDays = streak.currentStreak,
                        nexusScore = score.overallScore,
                    )
                }
                recalcWeeklyChange(score.overallScore)
                xpRepository.recordScoreSnapshot(score.overallScore)
            }.catch { Timber.e(it, "Badge evaluation failed") }.collect { }
        }
    }

    private fun observeScoreHistory() {
        viewModelScope.launch {
            xpRepository.getSnapshotsForUser()
                .catch { Timber.e(it, "Score history stream failed") }
                .collect { snapshots ->
                    snapshotsCache = snapshots
                    recalcWeeklyChange()
                }
        }
    }

    private fun recalcWeeklyChange(currentScore: Int = _uiState.value.nexusScore) {
        _uiState.update {
            it.copy(
                weeklyChange = weeklyChangeOf(
                    snapshots = snapshotsCache,
                    currentScore = currentScore,
                    now = System.currentTimeMillis(),
                ),
            )
        }
    }

    private fun loadPercentile() {
        viewModelScope.launch {
            val exact = leaderboardRepository.getGlobalRanking()
            val percentile = if (exact != null) {
                topPercentOf(exact.rank, exact.total)
            } else {
                val entries = leaderboardRepository.getLeaderboard(LeaderboardTab.GLOBAL)
                val me = entries.find { it.isCurrentUser }
                me?.let { topPercentOf(it.rank, entries.size) }
            }
            _uiState.update { it.copy(percentile = percentile?.takeIf { p -> p > 0 }) }
        }
    }

    fun unlock(id: String) {
        viewModelScope.launch { repository.unlock(id) }
    }

    fun dismissCelebration() {
        _uiState.update { it.copy(celebration = null) }
    }

    fun onAchievementShared(id: String) {
        viewModelScope.launch {
            xpRepository.awardXP(Constants.XP_PER_ACHIEVEMENT_SHARED, "achievement_shared")
        }
    }
}

private const val WEEK_MS = 7L * 24 * 60 * 60 * 1000

internal fun weeklyChangeOf(
    snapshots: List<ScoreSnapshot>,
    currentScore: Int,
    now: Long,
): Int {
    if (currentScore <= 0) return 0
    val windowStart = now - WEEK_MS
    val baseline = snapshots
        .filter { it.recordedAt in windowStart..now }
        .minByOrNull { it.recordedAt }
        ?: return 0
    return currentScore - baseline.score
}

