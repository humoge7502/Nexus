package com.nexus.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexus.common.ApiResult
import com.nexus.data.repository.ApplicationRepository
import com.nexus.data.repository.CertificationRepository
import com.nexus.data.repository.InternshipRepository
import com.nexus.data.repository.StreakRepository
import com.nexus.data.usecase.PlacementReadinessCalculator
import com.nexus.datastore.UserPreferences
import com.nexus.domain.model.Application
import com.nexus.domain.model.ApplicationStatus
import com.nexus.domain.model.DailyQuote
import com.nexus.domain.model.Internship
import com.nexus.domain.model.UserStats
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val userName: String = "",
    val stats: UserStats = UserStats(),
    /** The applications still moving through the pipeline, newest touch first. */
    val activeApplications: List<Application> = emptyList(),
    val todayOpportunities: List<Internship> = emptyList(),
    val streakDays: Int = 0,
    val dailyQuote: DailyQuote? = null,
    val careerNextStep: String = "",
    /** The quantified need behind [careerNextStep], kept separate so the hero
     *  card can render figures and words in their own registers. Empty when the
     *  caption is plain voice copy. */
    val careerNextStepMetric: String = "",
    val isOpportunitiesLoading: Boolean = true,
    val opportunitiesError: String? = null,
)

private data class UserSnapshot(
    val name: String,
    val streak: Int,
    val applicationCount: Int,
    val interviewCount: Int,
    val completedCerts: Int,
    val activeApplications: List<Application>,
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: InternshipRepository,
    private val applicationRepository: ApplicationRepository,
    private val certificationRepository: CertificationRepository,
    private val streakRepository: StreakRepository,
    private val readinessCalculator: PlacementReadinessCalculator,
    private val userPreferences: UserPreferences,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        recordStreakActivity()
        loadCareerReadiness()
        seedDailyQuote()
        observeUserData()
        observeOpportunities()
        refreshOpportunities()
    }

    /** Opening Home counts as today's activity; the repository is idempotent per day. */
    private fun recordStreakActivity() {
        viewModelScope.launch { streakRepository.checkAndUpdateStreaks() }
    }

    private fun loadCareerReadiness() {
        viewModelScope.launch {
            val readiness = readinessCalculator.calculate()
            val nextStep = readiness.actionItems.firstOrNull()
            _uiState.update {
                it.copy(
                    stats = it.stats.copy(careerReadinessScore = readiness.score / 100f),
                    // The caption carries the action in words; the figure that
                    // quantifies it travels in its own field so the card can
                    // set the two apart instead of burying the number in a
                    // sentence.
                    careerNextStep = when {
                        nextStep != null -> nextStep.title
                        readiness.score >= 80 -> "You're placement ready. Keep the momentum going."
                        else -> readiness.level
                    },
                    careerNextStepMetric = nextStep?.metric.orEmpty(),
                )
            }
        }
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
                streakRepository.currentStreak.map { it.currentStreak },
                applicationRepository.getAllApplications(),
                certificationRepository.getCompletedCertificationCount(),
            ) { name, streak, applications, certs ->
                UserSnapshot(
                    name = name,
                    streak = streak,
                    applicationCount = applications.size,
                    interviewCount = applications.count {
                        it.status == ApplicationStatus.INTERVIEW ||
                            it.status == ApplicationStatus.INTERVIEWING
                    },
                    completedCerts = certs,
                    activeApplications = applications
                        .filter { it.status in OPEN_STATUSES }
                        .sortedByDescending { it.updatedAt }
                        .take(HOME_APPLICATION_ROWS),
                )
            }.collect { snapshot ->
                _uiState.update {
                    it.copy(
                        userName = snapshot.name.ifBlank { DEFAULT_USER_NAME },
                        streakDays = snapshot.streak,
                        activeApplications = snapshot.activeApplications,
                        stats = it.stats.copy(
                            totalApplications = snapshot.applicationCount,
                            completedCerts = snapshot.completedCerts,
                            currentStreak = snapshot.streak,
                            interviewsScheduled = snapshot.interviewCount,
                        ),
                    )
                }
            }
        }
    }

    private fun observeOpportunities() {
        viewModelScope.launch {
            repository.getAllInternships().collect { internships ->
                val opportunities = internships.take(5)
                _uiState.update {
                    it.copy(
                        todayOpportunities = opportunities.ifEmpty { it.todayOpportunities },
                        // A cache hit resolves the loading state immediately;
                        // an empty cache keeps the skeleton up until the
                        // network call below settles, so users never see a
                        // flash of "nothing here" before data arrives.
                        isOpportunitiesLoading =
                            it.isOpportunitiesLoading && opportunities.isEmpty(),
                    )
                }
            }
        }
    }

    private fun refreshOpportunities() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isOpportunitiesLoading = it.todayOpportunities.isEmpty(),
                    opportunitiesError = null,
                )
            }
            when (val result = repository.refreshInternships()) {
                is ApiResult.Success -> _uiState.update {
                    it.copy(isOpportunitiesLoading = false, opportunitiesError = null)
                }
                is ApiResult.Error -> _uiState.update {
                    it.copy(
                        isOpportunitiesLoading = false,
                        // Keep cached opportunities visible; only surface the
                        // failure when there is nothing to show instead.
                        opportunitiesError =
                            if (it.todayOpportunities.isEmpty()) OFFLINE_MESSAGE else null,
                    )
                }
                ApiResult.Loading -> Unit
            }
        }
    }

    fun retryOpportunities() = refreshOpportunities()

    private companion object {
        const val DEFAULT_USER_NAME = "Future Engineer"
        const val OFFLINE_MESSAGE =
            "Couldn't reach the Sagitta network. Check your connection and try again."
        const val HOME_APPLICATION_ROWS = 3

        /** Outcomes are history, not a pipeline: the home strip only lists the
         *  applications a student still has to act on or wait on. */
        val OPEN_STATUSES = setOf(
            ApplicationStatus.APPLIED,
            ApplicationStatus.IN_REVIEW,
            ApplicationStatus.INTERVIEW,
            ApplicationStatus.INTERVIEWING,
            ApplicationStatus.OFFERED,
        )
    }
}
