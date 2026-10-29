package com.nexus.interviewlog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexus.data.repository.InterviewOutcomeRepository
import com.nexus.domain.model.InterviewOutcome
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class InterviewLogUiState(
  val isLoading: Boolean = true,
  val outcomes: List<InterviewOutcome> = emptyList(),
  val topCompanies: List<Pair<String, Int>> = emptyList(),
)

@HiltViewModel
class InterviewLogViewModel @Inject constructor(
  private val repository: InterviewOutcomeRepository,
) : ViewModel() {

  private val _uiState = MutableStateFlow(InterviewLogUiState())
  val uiState: StateFlow<InterviewLogUiState> = _uiState.asStateFlow()

  init {
    loadOutcomes()
  }

  private fun loadOutcomes() {
    viewModelScope.launch {
      repository.getAllOutcomes().collect { outcomes ->
        _uiState.update { it.copy(outcomes = outcomes, isLoading = false) }
      }
    }
    
    viewModelScope.launch {
      repository.getTopCompanies(5).collect { top ->
        _uiState.update { it.copy(topCompanies = top) }
      }
    }
  }

  fun deleteOutcome(id: String) {
    viewModelScope.launch {
      repository.delete(id)
    }
  }
}
