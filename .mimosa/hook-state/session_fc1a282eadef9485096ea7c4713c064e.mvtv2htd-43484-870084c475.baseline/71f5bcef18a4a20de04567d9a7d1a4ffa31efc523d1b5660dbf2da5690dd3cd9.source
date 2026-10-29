package com.nexus.goals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexus.data.repository.GoalRepository
import com.nexus.domain.model.Goal
import com.nexus.domain.model.UiState
import com.nexus.data.repository.UserXpRepository
import com.nexus.analytics.AnalyticsTracker
import com.nexus.common.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GoalsViewModel @Inject constructor(
    private val repository: GoalRepository,
    private val xpRepository: UserXpRepository,
    private val analyticsTracker: AnalyticsTracker,
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<List<Goal>>>(UiState.Loading)
    val uiState: StateFlow<UiState<List<Goal>>> = _uiState.asStateFlow()

    init {
        analyticsTracker.track("screen_view", mapOf("screen" to "goals"))
        loadGoals()
    }

    private fun loadGoals() {
        viewModelScope.launch {
            repository.getAllGoals()
                .map<List<Goal>, UiState<List<Goal>>> { UiState.Success(it) }
                .catch { e -> emit(UiState.Error(e.message ?: "Failed to load goals")) }
                .collect { _uiState.value = it }
        }
    }

    fun createGoal(goal: Goal) {
        viewModelScope.launch {
            repository.createGoal(goal)
            analyticsTracker.track("goal_created", mapOf("title" to goal.title, "category" to goal.category))
        }
    }

    fun updateProgress(id: String, progress: Float) {
        viewModelScope.launch {
            repository.updateProgress(id, progress)
        }
    }

    fun completeGoal(id: String) {
        viewModelScope.launch {
            repository.completeGoal(id)
            val result = xpRepository.awardXP(Constants.XP_PER_GOAL_COMPLETED, "goal_completed")
            analyticsTracker.track("goal_completed", mapOf("goal_id" to id, "xp_earned" to result.xpAwarded))
        }
    }

    fun deleteGoal(id: String) {
        viewModelScope.launch {
            repository.deleteGoal(id)
        }
    }
}
