package com.nexus.explore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexus.analytics.AnalyticsTracker
import com.nexus.data.repository.HackathonRepository
import com.nexus.domain.model.Hackathon
import com.nexus.domain.model.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HackathonDetailViewModel @Inject constructor(
    private val repository: HackathonRepository,
    private val analyticsTracker: AnalyticsTracker,
) : ViewModel() {

    private val _hackathons = MutableStateFlow<UiState<List<Hackathon>>>(UiState.Loading)
    val hackathons: StateFlow<UiState<List<Hackathon>>> = _hackathons.asStateFlow()

    init {
        viewModelScope.launch {
            repository.getAllHackathons()
                .map<List<Hackathon>, UiState<List<Hackathon>>> { UiState.Success(it) }
                .catch { e -> emit(UiState.Error(e.message ?: "Failed to load hackathons")) }
                .collect { _hackathons.value = it }
        }
    }

    fun toggleSave(id: String) {
        val current = (_hackathons.value as? UiState.Success)?.data?.firstOrNull { it.id == id } ?: return
        viewModelScope.launch {
            repository.toggleSave(id, !current.isSaved)
            analyticsTracker.track(
                "hackathon_saved",
                mapOf("hackathon_id" to id, "saved" to !current.isSaved),
            )
        }
    }
}
