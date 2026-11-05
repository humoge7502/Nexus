package com.nexus.onboarding

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexus.datastore.UserPreferences
import com.nexus.data.repository.UserXpRepository
import com.nexus.analytics.AnalyticsTracker
import com.nexus.common.Constants
import com.nexus.domain.repository.ISkillGraphRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
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
    val isSaving: Boolean = false,
    /** Set when the profile write failed; the screen keeps the user in setup. */
    val saveFault: String? = null,
)

val yearOptions = listOf("1st Year", "2nd Year", "3rd Year", "4th Year", "Graduate")
val skillOptions = listOf("Python", "Java", "JavaScript", "Kotlin", "C++", "SQL", "React", "Android", "Machine Learning", "Data Science", "Cloud Computing", "DevOps", "UI/UX", "Blockchain")
val interestOptions = listOf("Internships", "Full-Time Jobs", "Hackathons", "Research", "Open Source", "Freelancing", "Higher Studies", "Entrepreneurship")

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val userPreferences: UserPreferences,
    private val xpRepository: UserXpRepository,
    private val analyticsTracker: AnalyticsTracker,
    private val skillGraphRepository: ISkillGraphRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    /** null while DataStore is loading, so the boot flow does not flash onboarding. */
    val onboardingCompleted: StateFlow<Boolean?> = userPreferences.onboardingCompleted
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

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

    fun skipAndComplete(onComplete: () -> Unit) {
        if (_uiState.value.isSaving) return
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, saveFault = null) }
            // Defaults on an empty state are exactly the neutral skip profile.
            if (!persistProfile(OnboardingUiState(), "skip")) return@launch
            analyticsTracker.track("onboarding_skipped")
            _uiState.update { it.copy(isSaving = false) }
            onComplete()
        }
    }

    fun saveAndComplete(onComplete: () -> Unit) {
        if (_uiState.value.isSaving) return
        viewModelScope.launch {
            val state = _uiState.value
            _uiState.update { it.copy(isSaving = true, saveFault = null) }
            if (!persistProfile(state, "save")) return@launch

            // The profile is written from here on. These two can fail without
            // putting anyone back through setup, so they don't hold the flow.
            runCatching {
                skillGraphRepository.setDeclaredSkills(state.skills)
                xpRepository.awardXP(Constants.XP_ONBOARDING_COMPLETE, "onboarding")
            }.onFailure {
                analyticsTracker.track("onboarding_enrichment_failed", mapOf("path" to "save"))
            }

            analyticsTracker.track(
                "onboarding_complete",
                mapOf(
                    "role" to state.careerGoal,
                    "skills" to state.skills.size.toString(),
                    "interests" to state.interests.size.toString(),
                ),
            )
            _uiState.update { it.copy(isSaving = false) }
            onComplete()
        }
    }

    /** Returns false, with [OnboardingUiState.saveFault] set, when nothing was written. */
    private suspend fun persistProfile(state: OnboardingUiState, path: String): Boolean = try {
        userPreferences.saveOnboardingProfile(
            name = state.userName.ifBlank { "Student" },
            college = state.college.trim(),
            year = yearOptions.indexOf(state.academicYear).takeIf { it >= 0 }?.plus(1) ?: 1,
            skills = state.skills,
            interests = state.interests,
            careerGoal = state.careerGoal,
        )
        true
    } catch (e: CancellationException) {
        throw e
    } catch (e: Exception) {
        // Completing anyway would claim setup finished when nothing was saved,
        // and the next launch would ask for answers that were already typed.
        analyticsTracker.track("onboarding_save_failed", mapOf("path" to path))
        _uiState.update { it.copy(isSaving = false, saveFault = e.message.orEmpty()) }
        false
    }
}
