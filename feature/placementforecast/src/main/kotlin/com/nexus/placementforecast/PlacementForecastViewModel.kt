package com.nexus.placementforecast

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexus.data.repository.PlacementForecastRepository
import com.nexus.datastore.UserPreferences
import com.nexus.domain.model.PlacementForecast
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ForecastUiState(
  val isLoading: Boolean = true,
  val forecasts: List<PlacementForecast> = emptyList(),
  val error: String? = null,
)

@HiltViewModel
class PlacementForecastViewModel @Inject constructor(
  private val repository: PlacementForecastRepository,
  private val userPrefs: UserPreferences,
) : ViewModel() {

  private val _uiState = MutableStateFlow(ForecastUiState())
  val uiState: StateFlow<ForecastUiState> = _uiState.asStateFlow()

  init {
    loadForecasts()
  }

  private fun loadForecasts() {
    viewModelScope.launch {
      val userId = userPrefs.userId.first()
      repository.getForecasts(userId).collect { list ->
        _uiState.update { it.copy(forecasts = list, isLoading = false) }
      }
    }
  }

  fun refresh() {
    _uiState.update { it.copy(isLoading = true) }
    loadForecasts()
  }
}
