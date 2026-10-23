package com.nexus.profile

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexus.common.Constants
import com.nexus.data.repository.GitHubRepository
import com.nexus.data.repository.UserXpRepository
import com.nexus.datastore.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@Immutable
data class ProfileUiState(
    val isLoading: Boolean = true,
    val name: String = "",
    val university: String = "",
    val year: Int = 1,
    val bio: String = "",
    val nexusScore: Int = 0,
    val level: Int = 1,
    val totalXP: Int = 0,
    val streakDays: Int = 0,
    val applicationsCount: Int = 0,
    val certificationsCount: Int = 0,
    val githubUsername: String? = null,
    val linkedinUrl: String? = null,
    val referralCode: String = "",
    val referralLink: String = "",
    val error: String? = null,
    val xpToNextLevel: Int = 100,
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userPreferences: UserPreferences,
    private val xpRepository: UserXpRepository,
    private val gitHubRepository: GitHubRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    private var hasAwardedProfileXP = false

    init {
        loadProfile()
    }

    private fun loadProfile() {
        viewModelScope.launch {
            combine(
                userPreferences.displayName,
                userPreferences.userId,
                userPreferences.githubUsername,
                combine(
                    xpRepository.totalXP,
                    xpRepository.currentLevel,
                    xpRepository.xpToNextLevel,
                ) { totalXP, level, xpToNext -> Triple(totalXP, level, xpToNext) },
            ) { displayName, userId, githubUsername, xpData ->
                val referralCode = userId.takeLast(8).uppercase()
                if (!hasAwardedProfileXP && displayName.isNotBlank() && githubUsername.isNotBlank()) {
                    hasAwardedProfileXP = true
                    xpRepository.awardXP(Constants.XP_MAX_PROFILE, "profile_completed")
                }
                ProfileUiState(
                    isLoading = false,
                    name = displayName.ifBlank { "User" },
                    githubUsername = githubUsername.ifBlank { null },
                    level = xpData.second,
                    totalXP = xpData.first,
                    xpToNextLevel = xpData.third,
                    referralCode = referralCode,
                    referralLink = "https://nexusapp.in/referral/$referralCode",
                )
            }.collect { state -> _uiState.value = state }
        }
    }

    fun onReferralShared() {
        viewModelScope.launch {
            xpRepository.awardXP(Constants.XP_PER_REFERRAL, "referral_shared")
        }
    }
}
