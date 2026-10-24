package com.nexus.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexus.data.repository.ApplicationRepository
import com.nexus.data.repository.InternshipRepository
import com.nexus.datastore.UserPreferences
import com.nexus.domain.model.Certification
import com.nexus.domain.model.DailyQuote
import com.nexus.domain.model.Internship
import com.nexus.domain.model.UserStats
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val isLoading: Boolean = true,
    val userName: String = "",
    val stats: UserStats = UserStats(),
    val activeApplications: List<Internship> = emptyList(),
    val recommendedCertifications: List<Certification> = emptyList(),
    val todayOpportunities: List<Internship> = emptyList(),
    val streakDays: Int = 0,
    val dailyQuote: DailyQuote? = null,
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: InternshipRepository,
    private val applicationRepository: ApplicationRepository,
    private val userPreferences: UserPreferences,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        seedDailyQuote()
        observeUserData()
        loadOpportunities()
    }

    /** The quote is curated static content — a real product feature, not fake data. */
    private fun seedDailyQuote() {
        _uiState.update {
            it.copy(
                dailyQuote = DailyQuote(
                    text = "The only way to do great work is to love what you do.",
                    author = "Steve Jobs"
                ),
            )
        }
    }

    /** Real user identity + streak + tracked application count. */
    private fun observeUserData() {
        viewModelScope.launch {
            combine(
                userPreferences.displayName,
                userPreferences.studyStreak,
                applicationRepository.getAllApplications(),
            ) { name, streak, applications ->
                Triple(name, streak, applications)
            }.collect { (name, streak, applications) ->
                _uiState.update {
                    it.copy(
                        userName = name.ifBlank { DEFAULT_USER_NAME },
                        streakDays = streak,
                        stats = it.stats.copy(totalApplications = applications.size),
                    )
                }
            }
        }
    }

    private fun loadOpportunities() {
        viewModelScope.launch {
            repository.refreshInternships()
            repository.getAllInternships().collect { internships ->
                val opportunities = internships.take(5)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        todayOpportunities = opportunities.ifEmpty { it.todayOpportunities },
                    )
                }
            }
        }
    }

    private companion object {
        const val DEFAULT_USER_NAME = "Future Engineer"
    }
}
