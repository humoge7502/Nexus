package com.nexus.explore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.nexus.analytics.AnalyticsTracker
import com.nexus.common.Constants
import com.nexus.data.repository.CertificationRepository
import com.nexus.data.repository.HackathonRepository
import com.nexus.data.repository.InternshipRepository
import com.nexus.data.repository.UserXpRepository
import com.nexus.datastore.UserPreferences
import com.nexus.domain.model.Certification
import com.nexus.domain.model.Hackathon
import com.nexus.domain.model.Internship
import com.nexus.domain.model.Skill
import com.nexus.domain.model.UiState
import com.nexus.domain.usecase.OpportunityMatchingEngine
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import timber.log.Timber
import androidx.compose.runtime.Immutable
import javax.inject.Inject

@Immutable
data class ExploreUiState(
  val internships: UiState<List<Internship>> = UiState.Loading,
  val hackathons: UiState<List<Hackathon>> = UiState.Loading,
  val certifications: UiState<List<Certification>> = UiState.Loading,
  val filteredInternships: List<Internship> = emptyList(),
  val filteredHackathons: List<Hackathon> = emptyList(),
  val filteredCertifications: List<Certification> = emptyList(),
  val searchQuery: String = "",
  val selectedFilters: Set<String> = emptySet(),
  val selectedCategory: String = CATEGORY_INTERNSHIPS,
  /**
   * Fit percentage per internship id. An id is absent when the score cannot be
   * computed — the student has named no skills, or the posting asks for none —
   * so the card shows no chip rather than a number nobody derived.
   */
  val matchPercents: Map<String, Int> = emptyMap(),
) {
  val hasActiveSearch: Boolean get() = searchQuery.isNotBlank() || selectedFilters.isNotEmpty()
}

@HiltViewModel
class ExploreViewModel @Inject constructor(
  private val internshipRepository: InternshipRepository,
  private val hackathonRepository: HackathonRepository,
  private val certificationRepository: CertificationRepository,
  private val xpRepository: UserXpRepository,
  private val analyticsTracker: AnalyticsTracker,
  private val userPreferences: UserPreferences,
  private val matchingEngine: OpportunityMatchingEngine,
) : ViewModel() {

  private val _uiState = MutableStateFlow(ExploreUiState())
  val uiState: StateFlow<ExploreUiState> = _uiState.asStateFlow()

  private var searchJob: Job? = null

  /** Kept outside the state because it only annotates other results, never renders alone. */
  private var trackedSkills: List<Skill> = emptyList()

  val pagedInternships: Flow<PagingData<Internship>> = Pager(
    config = PagingConfig(
      pageSize = Constants.PAGE_SIZE,
      initialLoadSize = Constants.INITIAL_PAGE_SIZE,
      prefetchDistance = Constants.PREFETCH_DISTANCE,
    ),
    pagingSourceFactory = { ExploreInternshipPagingSource(internshipRepository) }
  ).flow.cachedIn(viewModelScope)

  init {
    analyticsTracker.track("screen_view", mapOf("screen" to "explore"))
    watchTrackedSkills()
    loadInternships()
    loadHackathons()
    loadCertifications()
  }

  private fun watchTrackedSkills() {
    viewModelScope.launch {
      userPreferences.userSkills.collect { names ->
        trackedSkills = names.sorted().map { Skill(name = it) }
        applyFilters()
      }
    }
  }

  private fun loadInternships() {
    viewModelScope.launch {
      internshipRepository.getAllInternships()
        .map<List<Internship>, UiState<List<Internship>>> { UiState.Success(it) }
        .catch { e -> emit(UiState.Error(e.message ?: "Failed to load internships")) }
        .collect { state ->
          _uiState.value = _uiState.value.copy(internships = state)
          applyFilters()
        }
    }
  }

  private fun loadHackathons() {
    viewModelScope.launch {
      hackathonRepository.getAllHackathons()
        .map<List<Hackathon>, UiState<List<Hackathon>>> { UiState.Success(it) }
        .catch { e -> emit(UiState.Error(e.message ?: "Failed to load hackathons")) }
        .collect { state ->
          _uiState.value = _uiState.value.copy(hackathons = state)
          applyFilters()
        }
    }
  }

  private fun loadCertifications() {
    viewModelScope.launch {
      certificationRepository.getAllCertifications()
        .map<List<Certification>, UiState<List<Certification>>> { UiState.Success(it) }
        .catch { e -> emit(UiState.Error(e.message ?: "Failed to load certifications")) }
        .collect { state ->
          _uiState.value = _uiState.value.copy(certifications = state)
          applyFilters()
        }
    }
  }

  private fun applyFilters() {
    val state = _uiState.value
    val query = state.searchQuery
    val filters = state.selectedFilters
    val category = state.selectedCategory

    val internships = when (val result = state.internships) {
      is UiState.Success -> result.data.filterInternshipsForExplore(query, filters, category)
      else -> emptyList()
    }

    _uiState.value = state.copy(
      filteredInternships = internships,
      filteredHackathons = when (val hackathons = state.hackathons) {
        is UiState.Success -> hackathons.data.filterHackathonsForExplore(query, filters, category)
        else -> emptyList()
      },
      filteredCertifications = when (val certifications = state.certifications) {
        is UiState.Success -> certifications.data.filterCertificationsForExplore(query, filters, category)
        else -> emptyList()
      },
      matchPercents = matchPercents(internships),
    )
  }

  /**
   * A percentage is only published when both sides of the comparison exist.
   * With no tracked skills there is nothing to compare against, and with no
   * required skills the posting never stated what it wants; [OpportunityMatchingEngine]
   * answers 0.5 in those cases as a neutral placeholder, and rendering that as
   * "50% match" would show a student a number no one computed.
   */
  private fun matchPercents(internships: List<Internship>): Map<String, Int> {
    if (trackedSkills.isEmpty()) return emptyMap()
    return internships.filter { it.requiredSkills.isNotEmpty() }
      .associate { internship ->
        internship.id to (matchingEngine.calculateMatchScore(internship, trackedSkills) * 100).toInt()
      }
  }

  fun refresh() {
    _uiState.value = _uiState.value.copy(
      internships = UiState.Loading,
      hackathons = UiState.Loading,
      certifications = UiState.Loading,
    )
    loadInternships()
    loadHackathons()
    loadCertifications()
  }

  fun selectCategory(category: String) {
    _uiState.value = _uiState.value.copy(selectedCategory = category)
    applyFilters()
    analyticsTracker.track("explore_category_selected", mapOf("category" to category))
  }

  fun updateSearchQuery(query: String) {
    _uiState.value = _uiState.value.copy(searchQuery = query)
    searchJob?.cancel()
    searchJob = viewModelScope.launch {
      delay(300)
      applyFilters()
      analyticsTracker.track("opportunity_searched", mapOf(
        "query_length" to query.length
      ))
    }
  }

  fun toggleFilter(filter: String) {
    val current = _uiState.value.selectedFilters
    _uiState.value = _uiState.value.copy(
      selectedFilters = if (filter in current) current - filter else current + filter,
    )
    applyFilters()
  }

  fun clearSearchAndFilters() {
    _uiState.value = _uiState.value.copy(
      searchQuery = "",
      selectedFilters = emptySet(),
    )
    applyFilters()
  }

  fun recordHackathonWin(hackathonId: String) {
    viewModelScope.launch {
      try {
        val result = xpRepository.awardXP(Constants.XP_PER_HACKATHON_WIN, "hackathon_win")
        analyticsTracker.track("hackathon_win", mapOf(
          "hackathon_id" to hackathonId,
          "xp_earned" to result.xpAwarded,
        ))
      } catch (e: Exception) {
        Timber.e(e, "Failed to record hackathon win")
      }
    }
  }
}
