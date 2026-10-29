package com.nexus.cohortintelligence

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexus.data.repository.CohortIntelligenceRepository
import com.nexus.datastore.UserPreferences
import com.nexus.domain.model.CohortEntry
import com.nexus.domain.repository.TierBreakdown
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
  val breakdown: List<TierBreakdown> = emptyList(),
  val placementRate: Float? = null,
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
      val college = userPrefs.userCollege.first()
      _uiState.update { it.copy(collegeId = college) }
      
      repository.getByCollege(college).collect { entries ->
        _uiState.update { it.copy(entries = entries, isLoading = false) }
      }
    }
    
    viewModelScope.launch {
      val college = userPrefs.userCollege.first()
      val breakdown = repository.getCollegeBreakdown(college)
      _uiState.update { it.copy(breakdown = breakdown) }
    }
    
    viewModelScope.launch {
      val college = userPrefs.userCollege.first()
      val rate = repository.getCollegePlacementRate(college)
      _uiState.update { it.copy(placementRate = rate) }
    }
  }
}
