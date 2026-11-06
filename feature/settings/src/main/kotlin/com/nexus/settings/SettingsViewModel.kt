package com.nexus.settings

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexus.analytics.AnalyticsTracker
import com.nexus.common.Constants
import com.nexus.data.repository.UserXpRepository
import com.nexus.data.usecase.DataExporter
import com.nexus.datastore.ExternalAccount
import com.nexus.datastore.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import android.net.Uri

@Immutable
data class SettingsUiState(
    val themeMode: String = "SYSTEM",
    val amoledEnabled: Boolean = false,
    val accentColor: String = "DEFAULT",
    val notificationsEnabled: Boolean = true,
    val leetcodeUsername: String = "",
    val githubUsername: String = "",
    val codeforcesUsername: String = "",
    val linkedinUrl: String = "",
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userPreferences: UserPreferences,
    private val analyticsTracker: AnalyticsTracker,
    private val xpRepository: UserXpRepository,
    private val dataExporter: DataExporter,
) : ViewModel() {

    /** One-shot outcome of an export request, consumed by the screen's snackbar. */
    private val _exportEvents = MutableSharedFlow<ExportEvent>(extraBufferCapacity = 1)
    val exportEvents: kotlinx.coroutines.flow.SharedFlow<ExportEvent> = _exportEvents.asSharedFlow()

    /** True while the export JSON is being assembled and written. */
    private val _exportInFlight = MutableStateFlow(false)
    val exportInFlight: StateFlow<Boolean> = _exportInFlight.asStateFlow()

    /**
     * Writes the export JSON to the document the student picked through the
     * Storage Access Framework. The file never transits the network or an
     * extra copy on disk — the picker is the whole permission model.
     */
    fun exportData(uri: Uri) {
        if (_exportInFlight.value) return
        viewModelScope.launch {
            _exportInFlight.value = true
            try {
                val result = dataExporter.exportToStream(uri)
                analyticsTracker.track(
                    "data_exported",
                    mapOf("outcome" to if (result.isSuccess) "exported" else "failed"),
                )
                _exportEvents.tryEmit(
                    if (result.isSuccess) ExportEvent.Exported else ExportEvent.Failed,
                )
            } finally {
                _exportInFlight.value = false
            }
        }
    }
    val uiState: StateFlow<SettingsUiState> = combine(
        userPreferences.themeMode,
        userPreferences.amoledEnabled,
        userPreferences.accentColor,
        userPreferences.notificationsEnabled,
        combine(
            userPreferences.leetcodeUsername,
            userPreferences.githubUsername,
            userPreferences.codeforcesUsername,
            userPreferences.linkedinUrl,
        ) { leetcode, github, codeforces, linkedin ->
            SettingsAccounts(leetcode, github, codeforces, linkedin)
        },
    ) { theme, amoled, accent, notifications, accounts ->
        SettingsUiState(
            themeMode = theme,
            amoledEnabled = amoled,
            accentColor = accent,
            notificationsEnabled = notifications,
            leetcodeUsername = accounts.leetcode,
            githubUsername = accounts.github,
            codeforcesUsername = accounts.codeforces,
            linkedinUrl = accounts.linkedin,
        )
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

    fun setAmoled(enabled: Boolean) {
        viewModelScope.launch {
            userPreferences.setAmoledEnabled(enabled)
            analyticsTracker.track("theme_changed", mapOf("amoled" to enabled.toString()))
        }
    }

    fun setAccentColor(color: String) {
        viewModelScope.launch {
            userPreferences.setAccentColor(color)
            analyticsTracker.track("accent_changed", mapOf("color" to color))
        }
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            userPreferences.saveNotificationsEnabled(enabled)
            analyticsTracker.track("notifications_toggled", mapOf("enabled" to enabled))
        }
    }

    /** Puts the walkthrough back on screen; the shell owns it, so it reappears on Home. */
    fun reopenTour() {
        viewModelScope.launch {
            userPreferences.reopenTour()
            analyticsTracker.track("tour_reopened")
        }
    }

    fun saveLeetcodeUsername(username: String) {
        viewModelScope.launch {
            userPreferences.saveExternalAccount(ExternalAccount.LEETCODE, username)
            xpRepository.awardXP(Constants.XP_PER_SKILL_ADDED, "leetcode_connected")
            analyticsTracker.track("leetcode_connected")
        }
    }

    fun saveGithubUsername(username: String) {
        viewModelScope.launch {
            userPreferences.saveExternalAccount(ExternalAccount.GITHUB, username)
            xpRepository.awardXP(Constants.XP_PER_SKILL_ADDED, "github_connected")
            analyticsTracker.track("github_connected")
        }
    }

    fun saveCodeforcesUsername(username: String) {
        viewModelScope.launch {
            userPreferences.saveExternalAccount(ExternalAccount.CODEFORCES, username)
            xpRepository.awardXP(Constants.XP_PER_SKILL_ADDED, "codeforces_connected")
            analyticsTracker.track("codeforces_connected")
        }
    }

    fun saveLinkedinUrl(url: String) {
        viewModelScope.launch {
            userPreferences.saveExternalAccount(ExternalAccount.LINKEDIN, url)
            xpRepository.awardXP(Constants.XP_PER_SKILL_ADDED, "linkedin_connected")
            analyticsTracker.track("linkedin_connected")
        }
    }

    private data class SettingsAccounts(
        val leetcode: String,
        val github: String,
        val codeforces: String,
        val linkedin: String,
    )
}

/** Outcome of a data-export request; the screen turns it into one snackbar. */
sealed interface ExportEvent {
    data object Exported : ExportEvent
    data object Failed : ExportEvent
}
