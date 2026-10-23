package com.nexus.achievements

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexus.analytics.AnalyticsTracker
import com.nexus.common.Constants
import com.nexus.data.repository.AchievementRepository
import com.nexus.data.repository.CertificationRepository
import com.nexus.data.repository.DSAProblemRepository
import com.nexus.data.repository.PortfolioRepository
import com.nexus.data.repository.StreakRepository
import com.nexus.data.repository.UserXpRepository
import com.nexus.domain.model.Achievement
import com.nexus.domain.model.AchievementType
import com.nexus.domain.model.UiState
import com.nexus.domain.usecase.BadgeUnlockEvaluator
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
    val percentile: Int = 0,
    val unlockedBadgeCount: Int = 0,
    val badgeCount: Int = 0,
    val streakDays: Int = 0,
)

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
) : ViewModel() {

    private val _uiState = MutableStateFlow(AchievementsUiState())
    val uiState: StateFlow<AchievementsUiState> = _uiState.asStateFlow()

    init {
        analyticsTracker.track("screen_view", mapOf("screen" to "achievements"))
        loadAchievements()
        loadUnlocked()
        observeXP()
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
                streakRepository.currentStreak,
                dsaProblemRepository.getSolvedCountFlow(),
                portfolioRepository.getProjectCount(),
            ) { streak, solved, projects ->
                _uiState.update { it.copy(streakDays = streak.currentStreak) }
                val certs = try { certificationRepository.getCertificationCount() } catch (_: Exception) { 0 }
                val xp = xpRepository.totalXP.first()
                val unlocked = repository.getUnlocked().first().map { it.type }.toSet()
                val stats = BadgeUnlockEvaluator.UserStats(
                    streakDays = streak.currentStreak,
                    problemsSolved = solved,
                    certificationsCompleted = certs,
                    applicationsSubmitted = 0,
                    projectsCompleted = projects,
                    totalXP = xp,
                )
                val newBadges = badgeEvaluator.evaluate(stats, unlocked)
                for (badge in newBadges) {
                    repository.unlock(badge.name.lowercase())
                    analyticsTracker.track("badge_unlocked", mapOf("badge" to badge.name))
                    Timber.d("Badge unlocked: ${badge.name}")
                }
            }.collect { }
        }
    }

    fun unlock(id: String) {
        viewModelScope.launch { repository.unlock(id) }
    }

    fun onAchievementShared(id: String) {
        viewModelScope.launch {
            xpRepository.awardXP(Constants.XP_PER_ACHIEVEMENT_SHARED, "achievement_shared")
        }
    }
}

