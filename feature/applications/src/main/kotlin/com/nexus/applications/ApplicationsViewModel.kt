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
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class ApplicationTab { ALL, APPLIED, INTERVIEWING, OUTCOMES }

@HiltViewModel
class ApplicationsViewModel @Inject constructor(
  private val applicationRepository: ApplicationRepository,
  private val nexusAnalytics: NexusAnalytics,
) : ViewModel() {

    private val selectedTab = MutableStateFlow(ApplicationTab.ALL)

    val uiState: StateFlow<ApplicationsUiState> = combine(
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
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ApplicationsUiState())

    fun selectTab(tab: ApplicationTab) { selectedTab.value = tab }

    fun updateStatus(applicationId: String, newStatus: ApplicationStatus) {
        viewModelScope.launch {
            applicationRepository.updateStatus(applicationId, newStatus)
if (newStatus == ApplicationStatus.OFFERED) {
      nexusAnalytics.trackOpportunityApplied(applicationId, "")
    }
        }
    }

init {
  nexusAnalytics.trackSessionStarted("applications", 0L)
}
}

@Immutable
data class ApplicationsUiState(
    val applications: List<Application> = emptyList(),
    val counts: ApplicationCounts = ApplicationCounts(0, 0, 0, 0, 0),
    val companyStats: List<CompanyApplicationStats> = emptyList(),
    val selectedTab: ApplicationTab = ApplicationTab.ALL,
)
