package com.nexus.internships

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexus.data.repository.ApplicationRepository
import com.nexus.data.repository.InternshipRepository
import com.nexus.datastore.UserPreferences
import com.nexus.domain.model.Internship
import com.nexus.domain.model.Skill
import com.nexus.domain.model.UiState
import com.nexus.domain.usecase.OpportunityMatchingEngine
import com.nexus.data.repository.UserXpRepository
import com.nexus.analytics.AnalyticsTracker
import com.nexus.common.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class InternshipViewModel @Inject constructor(
    private val repository: InternshipRepository,
    private val applicationRepository: ApplicationRepository,
    private val xpRepository: UserXpRepository,
    private val analyticsTracker: AnalyticsTracker,
    private val userPreferences: UserPreferences,
    private val matchingEngine: OpportunityMatchingEngine,
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<List<Internship>>>(UiState.Loading)
    val uiState: StateFlow<UiState<List<Internship>>> = _uiState.asStateFlow()

    private val _effect = MutableSharedFlow<InternshipEffect>()
    val effect: SharedFlow<InternshipEffect> = _effect.asSharedFlow()

    /** Opportunity ids the student already tracks, so the CTA can say so instead of firing twice. */
    private val _trackedIds = MutableStateFlow<Set<String>>(emptySet())
    val trackedIds: StateFlow<Set<String>> = _trackedIds.asStateFlow()

    /** Kept outside the state because it only feeds the score, never renders alone. */
    private var trackedSkills: List<Skill> = emptyList()

    init {
        analyticsTracker.track("screen_view", mapOf("screen" to "internships"))
        loadInternships()
        watchTrackedSkills()
        watchTrackedApplications()
    }

    private fun watchTrackedSkills() {
        viewModelScope.launch {
            userPreferences.userSkills.collect { names ->
                trackedSkills = names.sorted().map { Skill(name = it) }
            }
        }
    }

    private fun watchTrackedApplications() {
        viewModelScope.launch {
            applicationRepository.getAllApplications()
                .map { applications -> applications.map { it.opportunityId }.toSet() }
                .collect { _trackedIds.value = it }
        }
    }

    /**
     * Adds the role to My applications so the student can move it through the
     * saved → applied → interview → outcome ladder. The percentage is stored only
     * when both sides of the comparison exist: with no skills named by the student,
     * or none asked for by the posting, there is nothing to score, and [OpportunityMatchingEngine]
     * answers 0.5 in those cases as a neutral placeholder that would render as a
     * "50% match" nobody derived.
     */
    fun track(internship: Internship) {
        if (_trackedIds.value.contains(internship.id)) return
        val score = when {
            trackedSkills.isEmpty() || internship.requiredSkills.isEmpty() -> null
            else -> matchingEngine.calculateMatchScore(internship, trackedSkills)
        }
        viewModelScope.launch {
            applicationRepository.trackApplication(
                opportunityId = internship.id,
                company = internship.company.name,
                role = internship.title,
                type = internship.kind.name,
                matchScore = score,
            )
            _effect.emit(
                InternshipEffect.ShowSnackbar(
                    if (score == null) {
                        "Tracking in My applications. Set your skills to see a fit score."
                    } else {
                        "Tracking ${internship.company.name} in My applications."
                    }
                )
            )
        }
    }

    private fun loadInternships() {
        viewModelScope.launch {
            repository.getAllInternships()
                .map<List<Internship>, UiState<List<Internship>>> { UiState.Success(it) }
                .catch { e -> emit(UiState.Error(e.message ?: "Failed to load internships")) }
                .collect { _uiState.value = it }
        }
    }

    fun refresh() {
        _uiState.value = UiState.Loading
        viewModelScope.launch {
            val result = repository.refreshInternships()
            _uiState.value = when (result) {
                is com.nexus.common.ApiResult.Success -> UiState.Success(result.data)
                is com.nexus.common.ApiResult.Error -> UiState.Error(result.message)
                is com.nexus.common.ApiResult.Loading -> UiState.Loading
            }
        }
    }

    fun onApplyClicked(id: String) {
        viewModelScope.launch {
            val result = xpRepository.awardXP(Constants.XP_PER_APPLICATION, "internship_applied")
            analyticsTracker.track("internship_applied", mapOf("internship_id" to id, "xp_earned" to result.xpAwarded))
            if (result.leveledUp) {
                _effect.emit(InternshipEffect.ShowLevelUp(result.newLevel))
            }
        }
    }

    fun toggleSave(id: String) {
        viewModelScope.launch {
            try {
                val currentlySaved = repository.isSaved(id)
                val newSavedState = !currentlySaved
                repository.toggleSave(id, newSavedState)
                _uiState.update { state ->
                    when (state) {
                        is UiState.Success -> UiState.Success(
                            state.data.map { opp ->
                                if (opp.id == id) opp.copy(isSaved = newSavedState) else opp
                            }
                        )
                        else -> state
                    }
                }
                _effect.emit(
                    if (newSavedState) InternshipEffect.ShowSnackbar("Saved! Check your saved opportunities.")
                    else InternshipEffect.ShowSnackbar("Removed from saved.")
                )
            } catch (e: Exception) {
                Timber.e(e, "Failed to toggle save for opportunity: $id")
                _effect.emit(InternshipEffect.ShowError("Failed to save. Please try again."))
            }
        }
    }
}

sealed interface InternshipEffect {
    data class ShowSnackbar(val message: String) : InternshipEffect
    data class ShowError(val message: String) : InternshipEffect
    data class ShowLevelUp(val newLevel: Int) : InternshipEffect
}
