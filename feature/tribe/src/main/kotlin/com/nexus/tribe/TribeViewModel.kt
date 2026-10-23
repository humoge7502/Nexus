package com.nexus.tribe

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexus.analytics.NexusAnalytics
import com.nexus.datastore.UserPreferences
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
) : ViewModel() {

private val _uiState = MutableStateFlow(TribeUiState())
val uiState: StateFlow<TribeUiState> = _uiState.asStateFlow()

fun loadTribe() {
viewModelScope.launch {
val tribeId = userPrefs.userTribeId.first()
_uiState.update {
it.copy(isLoading = false, isInTribe = tribeId.isNotBlank(), tribeId = tribeId)
}
}
}

fun createOrJoinTribe(college: String, goal: String) {
viewModelScope.launch {
val tribeId = "tribe_${college.hashCode()}"
userPrefs.setUserTribeId(tribeId)
userPrefs.setUserCollege(college)
analytics.trackTribeJoined(college, 1)
_uiState.update {
it.copy(
isInTribe = true,
tribeId = tribeId,
memberCount = 1,
avgStreak = 0,
activeToday = 1,
recentActivity = emptyList()
)
}
}
}

fun leaveTribe() {
    viewModelScope.launch {
      userPrefs.setUserTribeId("")
      _uiState.update {
        it.copy(isInTribe = false, tribeId = "", memberCount = 0, avgStreak = 0,
          activeToday = 0, recentActivity = emptyList())
      }
    }
  }
}

data class TribeUiState(
  val isLoading: Boolean = true,
  val isInTribe: Boolean = false,
  val tribeId: String = "",
  val memberCount: Int = 0,
  val avgStreak: Int = 0,
  val activeToday: Int = 0,
  val recentActivity: List<String> = emptyList(),
)
