package com.nexus.community

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexus.common.ApiResult
import com.nexus.datastore.UserPreferences
import com.nexus.domain.model.Hackathon
import com.nexus.domain.repository.IOpportunityRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class CommunityUiState(
    val isLoading: Boolean = true,
    val college: String = "",
    val hackathons: List<Hackathon> = emptyList(),
    /** A read that failed, held apart from `hackathons.isEmpty()` so the screen
     *  can tell "no open hackathons" from "nothing could be read". */
    val error: String? = null,
)

@HiltViewModel
class CommunityViewModel @Inject constructor(
    private val repository: IOpportunityRepository,
    private val userPreferences: UserPreferences,
) : ViewModel() {

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _uiState = MutableStateFlow(CommunityUiState())
    val uiState: StateFlow<CommunityUiState> = _uiState.asStateFlow()

    /** A refresh that did not reach the feed. Transient, so it is an event and
     *  not a state the page keeps rendering. */
    private val _effect = MutableSharedFlow<CommunityEffect>()
    val effect: SharedFlow<CommunityEffect> = _effect.asSharedFlow()

    init {
        loadCommunity()
    }

    private fun loadCommunity() {
        viewModelScope.launch {
            combine(
                userPreferences.userCollege,
                repository.getHackathons(),
            ) { college, hackathons ->
                CommunityUiState(
                    isLoading = false,
                    college = college.trim(),
                    hackathons = hackathons.upcomingForCommunity(LocalDate.now(), MAX_HACKATHONS),
                )
            }
                .catch { e ->
                    // Without this the read fault cancels the collector and the
                    // page holds its skeleton for the rest of the session, so a
                    // failure is indistinguishable from a slow load.
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = e.message ?: "Failed to load the community feed",
                        )
                    }
                }
                .collect { state -> _uiState.value = state }
        }
    }

    /** Re-reads from a clean slate: the fault clears only when the data returns,
     *  and the batch plate is not allowed to assert anything in the meantime. */
    fun retry() {
        _uiState.value = CommunityUiState()
        loadCommunity()
    }

    fun refresh() {
        if (_isRefreshing.value) return
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                // The repository answers with a result, never an exception: the
                // old call dropped it, so a refresh that hit nothing looked
                // exactly like a refresh that succeeded.
                val result = repository.refreshOpportunities()
                if (result is ApiResult.Error) _effect.emit(CommunityEffect.RefreshFailed)
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                _effect.emit(CommunityEffect.RefreshFailed)
            } finally {
                // Without the finally a throw left the flag raised, and the
                // refresh action stayed disabled for the rest of the session.
                _isRefreshing.value = false
            }
        }
    }

    private companion object {
        const val MAX_HACKATHONS = 4
    }
}

sealed interface CommunityEffect {
    /** The feed was not reached; the hackathons on screen are the previous pull. */
    data object RefreshFailed : CommunityEffect
}

internal fun List<Hackathon>.upcomingForCommunity(today: LocalDate, limit: Int): List<Hackathon> =
    filter { !it.registrationDeadline.isBefore(today) }
        .sortedWith(compareByDescending<Hackathon> { it.isLive }.thenBy { it.registrationDeadline })
        .take(limit)
