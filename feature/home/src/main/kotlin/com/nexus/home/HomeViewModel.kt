package com.nexus.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexus.data.repository.InternshipRepository
import com.nexus.domain.model.Certification
import com.nexus.domain.model.DailyQuote
import com.nexus.domain.model.Internship
import com.nexus.domain.model.UserStats
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val isLoading: Boolean = true,
    val userName: String = "Krishna",
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
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadHardcodedData()
        loadSampleData()
    }

    private fun loadHardcodedData() {
        _uiState.update {
            it.copy(
                stats = UserStats(
                    totalApplications = 23,
                    completedCerts = 5,
                    currentStreak = 12,
                    interviewsScheduled = 3,
                    careerReadinessScore = 0.72f,
                ),
                streakDays = 12,
                dailyQuote = DailyQuote(
                    text = "The only way to do great work is to love what you do.",
                    author = "Steve Jobs"
                ),
            )
        }
    }

    private fun loadSampleData() {
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
}
