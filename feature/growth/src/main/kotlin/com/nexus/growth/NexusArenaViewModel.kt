package com.nexus.growth

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexus.data.repository.LeaderboardRepository
import com.nexus.data.repository.StreakRepository
import com.nexus.data.repository.UserXpRepository
import com.nexus.datastore.UserPreferences
import com.nexus.domain.model.LeaderboardEntry
import com.nexus.domain.repository.LeaderboardTab
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@Immutable
data class NexusArenaUiState(
    val isLoading: Boolean = true,
    val totalXp: Int = 0,
    val streakDays: Int = 0,
    val displayName: String = "",
    val college: String = "",
    val entries: List<LeaderboardEntry> = emptyList(),
    val isPublished: Boolean = false,
    val isPublishing: Boolean = false,
    val actionFailed: Boolean = false,
    val topPercent: Int = 0,
) {
    val currentUser: LeaderboardEntry?
        get() = entries.find { it.isCurrentUser }
}

internal fun topPercentOf(rank: Int, total: Int): Int =
    com.nexus.domain.usecase.topPercentOf(rank, total)

@HiltViewModel
class NexusArenaViewModel @Inject constructor(
    private val leaderboardRepository: LeaderboardRepository,
    userPreferences: UserPreferences,
    xpRepository: UserXpRepository,
    streakRepository: StreakRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(NexusArenaUiState())
    val uiState: StateFlow<NexusArenaUiState> = _uiState.asStateFlow()

    private val identityFlow = combine(
        userPreferences.displayName,
        userPreferences.userCollege,
        xpRepository.totalXP,
        streakRepository.currentStreak,
    ) { name, college, xp, streak ->
        ArenaIdentity(name, college, xp, streak.currentStreak)
    }

    init {
        viewModelScope.launch {
            identityFlow
                .catch { Timber.e(it, "Arena progress stream failed") }
                .collect { identity ->
                    _uiState.update {
                        it.copy(
                            displayName = identity.name,
                            college = identity.college,
                            totalXp = identity.xp,
                            streakDays = identity.streakDays,
                        )
                    }
                }
        }
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, actionFailed = false) }
            val published = leaderboardRepository.isPublicProfilePublished()
            if (published) syncPublishedProfile()
            reloadBoard(published)
        }
    }

    private suspend fun syncPublishedProfile() {
        runCatching {
            val identity = identityFlow.first()
            leaderboardRepository.publishProfile(
                displayName = identity.name,
                college = identity.college,
                totalXp = identity.xp,
                streakDays = identity.streakDays,
            )
        }.onFailure { Timber.e(it, "Failed to sync public profile score") }
    }

    fun joinLeaderboard() {
        if (_uiState.value.isPublishing) return
        viewModelScope.launch {
            _uiState.update { it.copy(isPublishing = true, actionFailed = false) }
            try {
                val identity = identityFlow.first()
                leaderboardRepository.publishProfile(
                    displayName = identity.name,
                    college = identity.college,
                    totalXp = identity.xp,
                    streakDays = identity.streakDays,
                )
                reloadBoard(published = true)
                _uiState.update { it.copy(isPublishing = false) }
            } catch (e: Exception) {
                Timber.e(e, "Failed to publish leaderboard profile")
                _uiState.update { it.copy(isPublishing = false, actionFailed = true) }
            }
        }
    }

    fun leaveLeaderboard() {
        if (_uiState.value.isPublishing) return
        viewModelScope.launch {
            _uiState.update { it.copy(isPublishing = true, actionFailed = false) }
            try {
                leaderboardRepository.removePublicProfile()
                reloadBoard(published = false)
                _uiState.update { it.copy(isPublishing = false) }
            } catch (e: Exception) {
                Timber.e(e, "Failed to remove public leaderboard profile")
                _uiState.update { it.copy(isPublishing = false, actionFailed = true) }
            }
        }
    }

    private suspend fun reloadBoard(published: Boolean) {
        val entries = leaderboardRepository.getLeaderboard(LeaderboardTab.GLOBAL)
        val ranking = if (published) leaderboardRepository.getGlobalRanking() else null
        val me = entries.find { it.isCurrentUser }
        _uiState.update {
            it.copy(
                isLoading = false,
                entries = entries,
                isPublished = published,
                topPercent = ranking?.let { r -> topPercentOf(r.rank, r.total) }
                    ?: topPercentOf(me?.rank ?: 0, entries.size),
            )
        }
    }

    private data class ArenaIdentity(
        val name: String,
        val college: String,
        val xp: Int,
        val streakDays: Int,
    )
}
