package com.nexus.internships

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexus.data.repository.InternshipRepository
import com.nexus.domain.model.Internship
import com.nexus.domain.model.UiState
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
    private val xpRepository: UserXpRepository,
    private val analyticsTracker: AnalyticsTracker,
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<List<Internship>>>(UiState.Loading)
    val uiState: StateFlow<UiState<List<Internship>>> = _uiState.asStateFlow()

    private val _effect = MutableSharedFlow<InternshipEffect>()
    val effect: SharedFlow<InternshipEffect> = _effect.asSharedFlow()

    init {
        analyticsTracker.track("screen_view", mapOf("screen" to "internships"))
        loadInternships()
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
