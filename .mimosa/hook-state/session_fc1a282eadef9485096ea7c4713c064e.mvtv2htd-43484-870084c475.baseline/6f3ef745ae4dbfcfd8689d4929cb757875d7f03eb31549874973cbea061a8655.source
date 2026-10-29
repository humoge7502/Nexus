package com.nexus.achievements

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexus.analytics.AnalyticsTracker
import com.nexus.data.repository.LeaderboardRepository
import com.nexus.domain.repository.LeaderboardTab
import com.nexus.domain.model.LeaderboardEntry
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@Immutable
data class LeaderboardUiState(
    val isLoading: Boolean = true,
    val selectedTab: LeaderboardTab = LeaderboardTab.GLOBAL,
    val entries: List<LeaderboardEntry> = emptyList(),
    val currentUser: LeaderboardEntry? = null,
)

@HiltViewModel
class LeaderboardViewModel @Inject constructor(
    private val repository: LeaderboardRepository,
    private val analyticsTracker: AnalyticsTracker,
) : ViewModel() {

    private val _uiState = MutableStateFlow(LeaderboardUiState())
    val uiState: StateFlow<LeaderboardUiState> = _uiState.asStateFlow()

    init {
        analyticsTracker.track("screen_view", mapOf("screen" to "leaderboard"))
        loadLeaderboard()
    }

    fun selectTab(tab: LeaderboardTab) {
        _uiState.update { it.copy(selectedTab = tab, isLoading = true) }
        loadLeaderboard()
    }

    private fun loadLeaderboard() {
        viewModelScope.launch {
            val tab = _uiState.value.selectedTab
            val entries = repository.getLeaderboard(tab)
            val currentUser = entries.find { it.isCurrentUser }
            _uiState.update {
                it.copy(
                    isLoading = false,
                    entries = entries,
                    currentUser = currentUser,
                )
            }
        }
    }

    fun refresh() {
        _uiState.update { it.copy(isLoading = true) }
        loadLeaderboard()
    }
}
