package com.nexus.onboarding

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexus.datastore.UserPreferences
import com.nexus.data.repository.UserXpRepository
import com.nexus.analytics.AnalyticsTracker
import com.nexus.common.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@Immutable
data class OnboardingUiState(
    val userName: String = "",
    val college: String = "",
    val academicYear: String = "",
    val skills: List<String> = emptyList(),
    val interests: List<String> = emptyList(),
    val careerGoal: String = "",
)

val yearOptions = listOf("1st Year", "2nd Year", "3rd Year", "4th Year", "Graduate")
val skillOptions = listOf("Python", "Java", "JavaScript", "Kotlin", "C++", "SQL", "React", "Android", "Machine Learning", "Data Science", "Cloud Computing", "DevOps", "UI/UX", "Blockchain")
val interestOptions = listOf("Internships", "Full-Time Jobs", "Hackathons", "Research", "Open Source", "Freelancing", "Higher Studies", "Entrepreneurship")

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val userPreferences: UserPreferences,
    private val xpRepository: UserXpRepository,
    private val analyticsTracker: AnalyticsTracker,
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    init {
        analyticsTracker.track("screen_view", mapOf("screen" to "onboarding"))
    }
    fun updateName(name: String) { _uiState.value = _uiState.value.copy(userName = name) }
    fun updateCollege(college: String) { _uiState.value = _uiState.value.copy(college = college) }
    fun updateYear(year: String) { _uiState.value = _uiState.value.copy(academicYear = year) }
    fun toggleSkill(skill: String) {
        val current = _uiState.value.skills.toMutableList()
        if (current.contains(skill)) current.remove(skill) else if (current.size < 5) current.add(skill)
        _uiState.value = _uiState.value.copy(skills = current)
    }
    fun toggleInterest(interest: String) {
        val current = _uiState.value.interests.toMutableList()
        if (current.contains(interest)) current.remove(interest) else current.add(interest)
        _uiState.value = _uiState.value.copy(interests = current)
    }
    fun updateCareerGoal(goal: String) { _uiState.value = _uiState.value.copy(careerGoal = goal) }

    fun saveAndComplete(onComplete: () -> Unit) {
        viewModelScope.launch {
            val state = _uiState.value
            userPreferences.saveDisplayName(state.userName.ifBlank { "Student" })
            userPreferences.setOnboardingCompleted()
            xpRepository.awardXP(Constants.XP_ONBOARDING_COMPLETE, "onboarding")
            analyticsTracker.track("onboarding_complete", mapOf("role" to state.careerGoal))
            onComplete()
        }
    }
}
