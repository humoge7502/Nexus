package com.nexus.settings

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexus.analytics.AnalyticsTracker
import com.nexus.common.Constants
import com.nexus.data.repository.UserXpRepository
import com.nexus.datastore.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@Immutable
data class SettingsUiState(
    val themeMode: String = "SYSTEM",
    val notificationsEnabled: Boolean = true,
    val leetcodeUsername: String = "",
    val githubUsername: String = "",
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userPreferences: UserPreferences,
    private val analyticsTracker: AnalyticsTracker,
    private val xpRepository: UserXpRepository,
) : ViewModel() {
    val uiState: StateFlow<SettingsUiState> = combine(
        userPreferences.themeMode,
        userPreferences.notificationsEnabled,
        userPreferences.leetcodeUsername,
        userPreferences.githubUsername,
    ) { theme, notifications, leetcode, github ->
        SettingsUiState(theme, notifications, leetcode, github)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SettingsUiState())

    init {
        analyticsTracker.trackScreenView("settings")
    }

    fun setDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            userPreferences.saveThemeMode(if (enabled) "DARK" else "LIGHT")
            analyticsTracker.track("theme_changed", mapOf("mode" to if (enabled) "DARK" else "LIGHT"))
        }
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            userPreferences.saveNotificationsEnabled(enabled)
            analyticsTracker.track("notifications_toggled", mapOf("enabled" to enabled))
        }
    }

    fun saveLeetcodeUsername(username: String) {
        viewModelScope.launch {
            userPreferences.saveLeetcodeUsername(username)
            xpRepository.awardXP(Constants.XP_PER_SKILL_ADDED, "leetcode_connected")
            analyticsTracker.track("leetcode_connected")
        }
    }

    fun saveGithubUsername(username: String) {
        viewModelScope.launch {
            userPreferences.saveGithubUsername(username)
            xpRepository.awardXP(Constants.XP_PER_SKILL_ADDED, "github_connected")
            analyticsTracker.track("github_connected")
        }
    }
}
