package com.nexus.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexus.analytics.AnalyticsTracker
import com.nexus.data.repository.InternshipRepository
import com.nexus.data.usecase.NexusSearchEngine
import com.nexus.domain.model.SearchMode
import com.nexus.domain.model.SearchQuery
import com.nexus.domain.model.SearchResult
import com.nexus.domain.model.Skill
import com.nexus.datastore.UserPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SearchUiState(
    val query: String = "",
    val results: List<SearchResult> = emptyList(),
    val isSearching: Boolean = false,
    val searchMode: SearchMode = SearchMode.HYBRID,
    val recentSearches: List<String> = emptyList(),
)

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val searchEngine: NexusSearchEngine,
    private val internshipRepository: InternshipRepository,
    private val userPreferences: UserPreferences,
    private val analyticsTracker: AnalyticsTracker,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    init {
        analyticsTracker.track("screen_view", mapOf("screen" to "search"))
    }

    fun updateQuery(query: String) {
        _uiState.value = _uiState.value.copy(query = query)
        searchJob?.cancel()
        if (query.length >= 2) {
            searchJob = viewModelScope.launch {
                delay(300)
                performSearch(query)
            }
        } else {
            _uiState.value = _uiState.value.copy(results = emptyList(), isSearching = false)
        }
    }

    fun setSearchMode(mode: SearchMode) {
        _uiState.value = _uiState.value.copy(searchMode = mode)
        val query = _uiState.value.query
        if (query.length >= 2) performSearch(query)
    }

    fun selectRecentSearch(query: String) {
        _uiState.value = _uiState.value.copy(query = query)
        performSearch(query)
    }

    private fun performSearch(query: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSearching = true)
            try {
                val internships = internshipRepository.getAllInternships().first()
                val skills = userPreferences.dataStore.data.first().let { prefs ->
                    emptyList<Skill>()
                }
                val searchQuery = SearchQuery(
                    queryText = query,
                    mode = _uiState.value.searchMode,
                )
                val results = searchEngine.search(searchQuery, internships, skills)
                _uiState.value = _uiState.value.copy(results = results, isSearching = false)
                analyticsTracker.track("search_performed", mapOf(
                    "query" to query,
                    "results" to results.size.toString(),
                    "mode" to _uiState.value.searchMode.name
                ))
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isSearching = false)
            }
        }
    }
}
