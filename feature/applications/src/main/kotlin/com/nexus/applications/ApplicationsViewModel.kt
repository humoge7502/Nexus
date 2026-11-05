package com.nexus.applications

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexus.analytics.NexusAnalytics
import com.nexus.analytics.AnalyticsTracker
import com.nexus.domain.repository.ApplicationCounts
import com.nexus.data.repository.ApplicationRepository
import com.nexus.database.dao.CompanyApplicationStats
import com.nexus.domain.model.Application
import com.nexus.domain.model.ApplicationStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class ApplicationTab { ALL, APPLIED, INTERVIEWING, OUTCOMES }

@HiltViewModel
class ApplicationsViewModel @Inject constructor(
  private val applicationRepository: ApplicationRepository,
  private val nexusAnalytics: NexusAnalytics,
) : ViewModel() {

    private val selectedTab = MutableStateFlow(ApplicationTab.ALL)

    private val _uiState = MutableStateFlow(ApplicationsUiState())
    val uiState: StateFlow<ApplicationsUiState> = _uiState.asStateFlow()

    /** Writes that did not land. Kept out of [uiState] because a toast is an
     *  event that passes, not a state the screen keeps rendering. */
    private val _effect = MutableSharedFlow<ApplicationsEffect>()
    val effect: SharedFlow<ApplicationsEffect> = _effect.asSharedFlow()

    private fun loadApplications() {
        viewModelScope.launch {
            combine(
                selectedTab,
                applicationRepository.getAllApplications(),
                applicationRepository.getApplicationCounts(),
                applicationRepository.getCompanyStats(),
            ) { tab, applications, counts, companyStats ->
                val filtered = when (tab) {
                    ApplicationTab.ALL -> applications
                    ApplicationTab.APPLIED -> applications.filter { it.status == ApplicationStatus.APPLIED }
                    ApplicationTab.INTERVIEWING -> applications.filter { it.status == ApplicationStatus.INTERVIEWING }
                    ApplicationTab.OUTCOMES -> applications.filter { it.status in listOf(
                        ApplicationStatus.OFFERED, ApplicationStatus.REJECTED, ApplicationStatus.ACCEPTED,
                    ) }
                }
                ApplicationsUiState(
                    applications = filtered,
                    counts = counts,
                    companyStats = companyStats,
                    selectedTab = tab,
                    isLoading = false,
                )
            }
                .catch { e ->
                    // Without this a failed read cancels the collector and the
                    // page keeps showing an empty list as though the student had
                    // applied to nothing. The counts stay unknown rather than 0.
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = e.message ?: "Failed to load applications",
                        )
                    }
                }
                .collect { state -> _uiState.value = state }
        }
    }

    /** Re-reads from a clean slate: the fault clears only when the data returns,
     *  and the tab the student was on is not reset under them. */
    fun retry() {
        _uiState.value = ApplicationsUiState(isLoading = true, selectedTab = selectedTab.value)
        loadApplications()
    }

    fun selectTab(tab: ApplicationTab) { selectedTab.value = tab }

    /** Moving a row along the ladder is a record the student believes they made.
     *  If the write does not land the row snaps back to its old stage, and only a
     *  toast can tell them that happened. */
    fun updateStatus(applicationId: String, newStatus: ApplicationStatus) {
        viewModelScope.launch {
            try {
                applicationRepository.updateStatus(applicationId, newStatus)
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                _effect.emit(ApplicationsEffect.StatusUpdateFailed)
            }
        }
    }

    /** Deletion reports itself in both directions: a row that stays on the list
     *  after the student removed it is the failure they cannot see for themselves. */
    fun remove(applicationId: String) {
        viewModelScope.launch {
            try {
                applicationRepository.delete(applicationId)
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                _effect.emit(ApplicationsEffect.RemoveFailed)
            }
        }
    }

init {
  nexusAnalytics.trackSessionStarted("applications", 0L)
  loadApplications()
}
}

@Immutable
data class ApplicationsUiState(
    val applications: List<Application> = emptyList(),
    val counts: ApplicationCounts = ApplicationCounts(0, 0, 0, 0, 0),
    val companyStats: List<CompanyApplicationStats> = emptyList(),
    val selectedTab: ApplicationTab = ApplicationTab.ALL,
    val isLoading: Boolean = true,
    /** A read that failed, held apart from `applications.isEmpty()` so the screen
     *  can tell "nothing tracked yet" from "nothing could be read". Only a fresh
     *  emission from the read clears it, so a fault never silently becomes an
     *  empty list. */
    val error: String? = null,
)

sealed interface ApplicationsEffect {
    /** The stage never moved; the row still carries the status it had before. */
    data object StatusUpdateFailed : ApplicationsEffect

    /** The row is still on the list because the delete never reached the database. */
    data object RemoveFailed : ApplicationsEffect
}
