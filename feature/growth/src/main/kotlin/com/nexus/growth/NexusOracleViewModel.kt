package com.nexus.growth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexus.analytics.AnalyticsTracker
import com.nexus.designsystem.component.IntelligenceCardData
import com.nexus.designsystem.component.IntelligenceCardDefaults
import com.nexus.network.ai.AIOpenRouterService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import androidx.compose.runtime.Immutable
import javax.inject.Inject

@Immutable
data class OracleUiState(
    val isLoading: Boolean = false,
    /** The reading is AI-generated prose, not a measurement of this user's data. */
    val hasGenerated: Boolean = false,
    val intelligenceCards: List<IntelligenceCardData> = emptyList(),
    val opportunityTitle: String = "",
    val opportunityBody: String = "",
    val opportunityCta: String = "",
    val error: String? = null,
)

@HiltViewModel
class NexusOracleViewModel @Inject constructor(
    private val aiService: AIOpenRouterService,
    private val analyticsTracker: AnalyticsTracker,
) : ViewModel() {

    private val _uiState = MutableStateFlow(OracleUiState())
    val uiState: StateFlow<OracleUiState> = _uiState.asStateFlow()

    init {
        analyticsTracker.track("screen_view", mapOf("screen" to "oracle"))
    }

    fun loadOracle() {
        if (_uiState.value.isLoading) return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val cardsPrompt = "Give 3 short, practical next-step suggestions for a computer " +
                    "science student preparing for campus placements. One per line. Line 1: what to " +
                    "study next. Line 2: a habit worth fixing. Line 3: something already going well. " +
                    "No percentages, no invented statistics, no company names."
                val cardsResponse = aiService.sendMessage(cardsPrompt)
                val cardLines = cardsResponse.lines().filter { it.isNotBlank() }.take(3)
                val cards = listOfNotNull(
                    cardLines.getOrNull(0)?.let { IntelligenceCardDefaults.oracleCard(it) },
                    cardLines.getOrNull(1)?.let { IntelligenceCardDefaults.mirrorCard(it) },
                    cardLines.getOrNull(2)?.let { IntelligenceCardDefaults.momentumCard(it) },
                )

                val oppPrompt = "Describe one kind of early-career opportunity a CS student should " +
                    "look for this month, in plain language. Format: Title|Why it matters|What to do " +
                    "next. Do not invent deadlines, match scores or company names."
                val oppResponse = aiService.sendMessage(oppPrompt)
                val oppParts = oppResponse.split("|")

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    hasGenerated = true,
                    intelligenceCards = cards,
                    opportunityTitle = oppParts.getOrElse(0) { "" }.trim(),
                    opportunityBody = oppParts.getOrElse(1) { "" }.trim(),
                    opportunityCta = oppParts.getOrElse(2) { "" }.trim(),
                    error = if (cards.isEmpty()) "The oracle didn't return anything readable." else null,
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "The oracle couldn't reach the model.",
                )
            }
        }
    }
}
