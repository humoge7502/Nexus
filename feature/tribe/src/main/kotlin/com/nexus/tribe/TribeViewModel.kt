package com.nexus.tribe

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexus.analytics.NexusAnalytics
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

@HiltViewModel
class TribeViewModel @Inject constructor(
  private val userPrefs: UserPreferences,
  private val analytics: NexusAnalytics,
  private val cohortRepository: CohortIntelligenceRepository,
) : ViewModel() {

  private val _uiState = MutableStateFlow(TribeUiState())
  val uiState: StateFlow<TribeUiState> = _uiState.asStateFlow()

  fun loadTribe() {
    viewModelScope.launch {
      val tribeId = userPrefs.userTribeId.first()
      val college = userPrefs.userCollege.first()
      val goal = userPrefs.tribeGoal.first()
      val podSize = userPrefs.tribePodSize.first()
      val cohort = runCatching { latestCohort(college) }.getOrNull()
      _uiState.update {
        it.copy(
          isLoading = false,
          isInTribe = tribeId.isNotBlank(),
          tribeId = tribeId,
          college = college,
          goal = goal,
          podSize = podSize,
          cohort = cohort,
        )
      }
    }
  }

  fun createOrJoinTribe(college: String, goal: String, podSize: Int = 5) {
    val trimmed = college.trim()
    if (trimmed.isBlank()) {
      _uiState.update { it.copy(error = "Enter your college to find your tribe.") }
      return
    }
    viewModelScope.launch {
      _uiState.update { it.copy(isLoading = true, error = null) }
      val tribeId = "tribe_" + trimmed.lowercase()
        .replace(Regex("[^a-z0-9]+"), "_")
        .trim('_')
        .take(48)
      val cohort = runCatching { latestCohort(trimmed) }.getOrNull()
      userPrefs.setUserTribeId(tribeId)
      userPrefs.setUserCollege(trimmed)
      userPrefs.saveTribeSetup(goal, podSize)
      analytics.trackTribeJoined(trimmed, podSize)
      _uiState.update {
        it.copy(
          isLoading = false,
          isInTribe = true,
          tribeId = tribeId,
          college = trimmed,
          goal = goal.trim(),
          podSize = podSize.coerceIn(3, 7),
          cohort = cohort,
        )
      }
    }
  }

  fun leaveTribe() {
    viewModelScope.launch {
      userPrefs.setUserTribeId("")
      _uiState.update {
        it.copy(
          isInTribe = false,
          tribeId = "",
          cohort = null,
          error = null,
        )
      }
    }
  }

  // College-level cohorts are written by the intelligence backend; showing
  // them is honest peer data, unlike invented member counters.
  private suspend fun latestCohort(college: String): CohortEntry? {
    if (college.isBlank()) return null
    return cohortRepository.getByCollege(college).first().maxByOrNull { it.updatedAt }
  }
}

data class TribeUiState(
  val isLoading: Boolean = true,
  val isInTribe: Boolean = false,
  val tribeId: String = "",
  val college: String = "",
  val goal: String = "",
  val podSize: Int = 5,
  val cohort: CohortEntry? = null,
  val error: String? = null,
)
