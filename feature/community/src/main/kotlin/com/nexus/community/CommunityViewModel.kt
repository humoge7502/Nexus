package com.nexus.community

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexus.datastore.UserPreferences
import com.nexus.domain.model.Hackathon
import com.nexus.domain.repository.IOpportunityRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class CommunityUiState(
    val isLoading: Boolean = true,
    val college: String = "",
    val hackathons: List<Hackathon> = emptyList(),
)

@HiltViewModel
class CommunityViewModel @Inject constructor(
    private val repository: IOpportunityRepository,
    userPreferences: UserPreferences,
) : ViewModel() {

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    val uiState: StateFlow<CommunityUiState> = combine(
        userPreferences.userCollege,
        repository.getHackathons(),
    ) { college, hackathons ->
        CommunityUiState(
            isLoading = false,
            college = college.trim(),
            hackathons = hackathons.upcomingForCommunity(LocalDate.now(), MAX_HACKATHONS),
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = CommunityUiState(),
    )

    fun refresh() {
        if (_isRefreshing.value) return
        viewModelScope.launch {
            _isRefreshing.value = true
            repository.refreshOpportunities()
            _isRefreshing.value = false
        }
    }

    private companion object {
        const val MAX_HACKATHONS = 4
    }
}

internal fun List<Hackathon>.upcomingForCommunity(today: LocalDate, limit: Int): List<Hackathon> =
    filter { !it.registrationDeadline.isBefore(today) }
        .sortedWith(compareByDescending<Hackathon> { it.isLive }.thenBy { it.registrationDeadline })
        .take(limit)
