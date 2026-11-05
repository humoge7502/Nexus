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
      try {
        val tribeId = userPrefs.userTribeId.first()
        val college = userPrefs.userCollege.first()
        val goal = userPrefs.tribeGoal.first()
        val podSize = userPrefs.tribePodSize.first()
        // A failed cohort read is its own event: the tribe itself loaded, only the
        // peer benchmark is missing. Dropping the exception made an outage read as
        // "not enough students on Sagitta yet", so the fault is kept and shown.
        val cohortRead = runCatching { latestCohort(college) }
        _uiState.update {
          it.copy(
            isLoading = false,
            loadError = null,
            isInTribe = tribeId.isNotBlank(),
            tribeId = tribeId,
            college = college,
            goal = goal,
            podSize = podSize,
            cohort = cohortRead.getOrNull(),
            cohortError = cohortRead.exceptionOrNull()?.let { e ->
              e.message ?: "Couldn't read the campus cohort."
            },
          )
        }
      } catch (e: Exception) {
        // The preference reads used to throw straight out of the coroutine while
        // isLoading was still true, which left the screen spinning on a dead load.
        _uiState.update {
          it.copy(isLoading = false, loadError = e.message ?: "Failed to load your tribe.")
        }
      }
    }
  }

  fun retry() {
    _uiState.update { it.copy(isLoading = true, loadError = null, cohortError = null) }
    loadTribe()
  }

  fun createOrJoinTribe(college: String, goal: String, podSize: Int = 5) {
    val trimmed = college.trim()
    if (trimmed.isBlank()) {
      _uiState.update { it.copy(error = "Enter your college to find your tribe.") }
      return
    }
    viewModelScope.launch {
      _uiState.update { it.copy(isLoading = true, error = null) }
      try {
        val tribeId = "tribe_" + trimmed.lowercase()
          .replace(Regex("[^a-z0-9]+"), "_")
          .trim('_')
          .take(48)
        val cohortRead = runCatching { latestCohort(trimmed) }
        userPrefs.setUserTribeId(tribeId)
        userPrefs.setUserCollege(trimmed)
        userPrefs.saveTribeSetup(goal, podSize)
        analytics.trackTribeJoined(trimmed, podSize)
        _uiState.update {
          it.copy(
            isLoading = false,
            loadError = null,
            isInTribe = true,
            tribeId = tribeId,
            college = trimmed,
            goal = goal.trim(),
            podSize = podSize.coerceIn(3, 7),
            cohort = cohortRead.getOrNull(),
            cohortError = cohortRead.exceptionOrNull()?.let { e ->
              e.message ?: "Couldn't read the campus cohort."
            },
          )
        }
      } catch (e: Exception) {
        // A rejected write left isLoading true forever, so the pod was neither
        // joined nor refused and the screen said nothing about which.
        _uiState.update {
          it.copy(isLoading = false, error = e.message ?: "Couldn't save your tribe.")
        }
      }
    }
  }

  fun leaveTribe() {
    viewModelScope.launch {
      try {
        userPrefs.setUserTribeId("")
        _uiState.update {
          it.copy(
            isInTribe = false,
            tribeId = "",
            cohort = null,
            cohortError = null,
            error = null,
          )
        }
      } catch (e: Exception) {
        // Without this a rejected write simply did nothing on screen, so the pod
        // looked like it was still joined with no reason given.
        _uiState.update {
          it.copy(error = e.message ?: "Couldn't leave your tribe.")
        }
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
  /** The tribe itself could not be read at all. Kept apart from [error], which is
   *  a form message, so a failed load can be shown instead of "no tribe yet". */
  val loadError: String? = null,
  /** Peer benchmarks failed to read. Distinct from a [cohort] that is simply null,
   *  which means no cohort has been published for this college yet. */
  val cohortError: String? = null,
)
