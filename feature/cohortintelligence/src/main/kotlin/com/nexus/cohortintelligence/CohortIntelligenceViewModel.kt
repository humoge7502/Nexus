package com.nexus.cohortintelligence

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexus.data.repository.CohortIntelligenceRepository
import com.nexus.datastore.UserPreferences
import com.nexus.domain.model.CohortEntry
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CohortUiState(
  val isLoading: Boolean = true,
  val collegeId: String = "",
  val entries: List<CohortEntry> = emptyList(),
  val placementRate: Float? = null,
  val error: String? = null,
)

@HiltViewModel
class CohortIntelligenceViewModel @Inject constructor(
  private val repository: CohortIntelligenceRepository,
  private val userPrefs: UserPreferences,
) : ViewModel() {

  private val _uiState = MutableStateFlow(CohortUiState())
  val uiState: StateFlow<CohortUiState> = _uiState.asStateFlow()

  fun loadData() {
    viewModelScope.launch {
      _uiState.update { it.copy(isLoading = true, error = null) }
      val college = userPrefs.userCollege.first()
      _uiState.update { it.copy(collegeId = college) }

      if (college.isBlank()) {
        // Cohort rows are keyed by college. Querying an empty key returns an
        // empty list, which the screen would render as "your cohort has placed
        // nobody" — a statement about people we never looked up.
        _uiState.update {
          it.copy(isLoading = false, entries = emptyList(), placementRate = null)
        }
        return@launch
      }

      try {
        val rate = repository.getCollegePlacementRate(college)
        repository.getByCollege(college).collect { entries ->
          _uiState.update {
            it.copy(
              entries = entries,
              placementRate = rate,
              isLoading = false,
              error = null,
            )
          }
        }
      } catch (e: Exception) {
        // A faulted read used to leave isLoading true forever: the screen kept
        // its skeleton spinning and never said why nothing arrived.
        _uiState.update {
          it.copy(
            isLoading = false,
            error = e.message ?: "Cohort records could not be read.",
          )
        }
      }
    }
  }
}
