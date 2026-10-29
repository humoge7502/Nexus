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
    val isLoading: Boolean = true,
    val forecast30Day: String = "--",
    val forecast30Projected: String = "--",
    val forecast30Note: String = "",
    val forecast90Day: String = "--",
    val forecast90Projected: String = "--",
    val forecast90Note: String = "",
    val intelligenceCards: List<IntelligenceCardData> = emptyList(),
    val skills: List<Triple<String, Float, Float>> = emptyList(),
    val careerPaths: List<Pair<String, Float>> = emptyList(),
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
        loadOracle()
    }

    fun loadOracle() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val forecastPrompt = "Give me a career forecast for a CS student. Format: 30DAY_CURRENT|30DAY_PROJECTED|30DAY_NOTE|90DAY_CURRENT|90DAY_PROJECTED|90DAY_NOTE. Use realistic numbers."
                val forecast = aiService.sendMessage(forecastPrompt)
                val forecastParts = forecast.split("|")

                val cardsPrompt = "Generate 4 short actionable career oracle insights for a CS student. Each on a new line. Cover: study recommendation, opportunity alert, skill gap warning, and motivation."
                val cardsResponse = aiService.sendMessage(cardsPrompt)
                val cardLines = cardsResponse.lines().filter { it.isNotBlank() }.take(4)
                val cards = listOf(
                    if (cardLines.size > 0) IntelligenceCardDefaults.oracleCard(cardLines[0]) else IntelligenceCardDefaults.oracleCard(""),
                    if (cardLines.size > 1) IntelligenceCardDefaults.signalCard(cardLines[1], 85) else IntelligenceCardDefaults.signalCard("", 85),
                    if (cardLines.size > 2) IntelligenceCardDefaults.mirrorCard(cardLines[2]) else IntelligenceCardDefaults.mirrorCard(""),
                    if (cardLines.size > 3) IntelligenceCardDefaults.momentumCard(cardLines[3]) else IntelligenceCardDefaults.momentumCard(""),
                )

                val skillsPrompt = "List 5 tech skills for a CS student with current proficiency (0-100) and projected proficiency (0-100). Format as: Skill|current|projected on each line."
                val skillsResponse = aiService.sendMessage(skillsPrompt)
                val skillLines = skillsResponse.lines().filter { it.isNotBlank() }.take(5)
                val skills = skillLines.mapNotNull { line ->
                    val parts = line.split("|")
                    if (parts.size >= 3) Triple(parts[0].trim(), (parts[1].trim().toFloatOrNull() ?: 50f) / 100f, (parts[2].trim().toFloatOrNull() ?: 70f) / 100f)
                    else null
                }

                val pathsPrompt = "List 4 career paths for a CS student with probability percentages. Format as: Path|percentage on each line. Use realistic numbers that sum to ~100."
                val pathsResponse = aiService.sendMessage(pathsPrompt)
                val pathLines = pathsResponse.lines().filter { it.isNotBlank() }.take(4)
                val paths = pathLines.mapNotNull { line ->
                    val parts = line.split("|")
                    if (parts.size >= 2) Pair(parts[0].trim(), (parts[1].trim().toFloatOrNull() ?: 25f) / 100f)
                    else null
                }

                val oppPrompt = "Generate a short opportunity alert for a CS student. Format: Title|Body|Call to action. Make it specific and timely."
                val oppResponse = aiService.sendMessage(oppPrompt)
                val oppParts = oppResponse.split("|")

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    forecast30Day = forecastParts.getOrElse(0) { "--" },
                    forecast30Projected = forecastParts.getOrElse(1) { "--" },
                    forecast30Note = forecastParts.getOrElse(2) { "" },
                    forecast90Day = forecastParts.getOrElse(3) { "--" },
                    forecast90Projected = forecastParts.getOrElse(4) { "--" },
                    forecast90Note = forecastParts.getOrElse(5) { "" },
                    intelligenceCards = cards.ifEmpty { defaultCards() },
                    skills = skills.ifEmpty { defaultSkills() },
                    careerPaths = paths.ifEmpty { defaultPaths() },
                    opportunityTitle = oppParts.getOrElse(0) { "Opportunity Intel" },
                    opportunityBody = oppParts.getOrElse(1) { "" },
                    opportunityCta = oppParts.getOrElse(2) { "" },
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    private fun defaultCards() = listOf(
        IntelligenceCardDefaults.oracleCard("If you study System Design for 2 hours today, you'll be interview-ready for Amazon in 23 days vs. 41 days at current pace."),
        IntelligenceCardDefaults.signalCard("Microsoft Explore Intern opens TODAY. You: 91% match. Competition: Lower than average.", 91),
        IntelligenceCardDefaults.mirrorCard("You haven't practiced graphs in 18 days. Graph problems appear in 34% of FAANG rounds."),
        IntelligenceCardDefaults.momentumCard("Your last 5 coding sessions were above average. This is the time to tackle your hardest goal."),
    )

    private fun defaultSkills() = listOf(
        Triple("System Design", 0.35f, 0.72f),
        Triple("DSA", 0.78f, 0.88f),
        Triple("Web Dev", 0.65f, 0.80f),
        Triple("AI/ML", 0.35f, 0.55f),
        Triple("Cloud", 0.50f, 0.68f),
    )

    private fun defaultPaths() = listOf(
        "SWE at product company" to 0.68f,
        "ML Engineer role" to 0.45f,
        "Startup founding team" to 0.28f,
        "Research/PhD path" to 0.21f,
    ).map { it.first to it.second.coerceAtMost(1f) }
}
