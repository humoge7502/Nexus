package com.nexus.interviewlog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexus.data.repository.InterviewOutcomeRepository
import com.nexus.domain.model.InterviewOutcome
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class InterviewLogUiState(
  val isLoading: Boolean = true,
  val outcomes: List<InterviewOutcome> = emptyList(),
  val topCompanies: List<Pair<String, Int>> = emptyList(),
  /** A read that failed, kept apart from `outcomes.isEmpty()` so the screen can
   *  tell "nothing logged yet" from "nothing could be read". */
  val error: String? = null,
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
      repository.getAllOutcomes()
        .catch { e ->
          // Without this the exception kills the collector and the page sits on
          // its skeleton for the rest of the session.
          _uiState.update {
            it.copy(isLoading = false, error = e.message ?: "Failed to load your interview log")
          }
        }
        .collect { outcomes ->
          _uiState.update { it.copy(outcomes = outcomes, isLoading = false, error = null) }
        }
    }

    viewModelScope.launch {
      repository.getTopCompanies(5)
        .catch { e ->
          // The statistics plate is a count of rounds, so a failed read is a
          // fault in the page, not a plate that may simply stay blank.
          _uiState.update {
            it.copy(isLoading = false, error = e.message ?: "Failed to load your interview statistics")
          }
        }
        .collect { top ->
          // No `error = null` here: only the list flow clears the fault, because
          // only its reload proves the data the fault was about is back.
          _uiState.update { it.copy(topCompanies = top) }
        }
    }
  }

  fun retry() {
    _uiState.value = InterviewLogUiState()
    loadOutcomes()
  }

  fun deleteOutcome(id: String) {
    viewModelScope.launch {
      repository.delete(id)
    }
  }
}
