package com.nexus.ai

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nexus.common.AIRateLimiter
import com.nexus.common.RateLimitResult
import com.nexus.datastore.UserPreferences
import com.nexus.network.ai.AIOpenRouterService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Below this the paste is a fragment rather than a document, and a score earned
 * from a fragment would be a number about nothing we actually read.
 */
private const val MIN_READABLE_CHARS = 300

private const val SCHEMA_PROMPT =
    "You score resumes for applicant-tracking systems used by Indian tech employers. " +
        "Reply with JSON only, no prose, in exactly this shape: " +
        "{\"ats_score\": 0-100, " +
        "\"sections\": [{\"name\": string, \"score\": 0-100, \"note\": one short sentence}], " +
        "\"missing_keywords\": [string], \"recommendations\": [string]}. " +
        "Include one section for each of: Contact Info, Education, Skills, Experience, " +
        "Quantified Results, Keywords. Score only what the supplied text proves; an absent " +
        "section scores 0 with a note saying it is absent. Never invent an achievement, " +
        "employer, date or metric the text does not contain."

data class ResumeSectionScore(
    val name: String,
    val score: Int,
    val note: String,
)

data class ResumeAnalysis(
    val atsScore: Int,
    val sections: List<ResumeSectionScore>,
    val missingKeywords: List<String>,
    val recommendations: List<String>,
    val charactersRead: Int,
    val charactersGiven: Int,
)

@Immutable
data class ResumeAnalyzerUiState(
    val isLoading: Boolean = false,
    val resumeText: String = "",
    val targetRole: String = "",
    val analysis: ResumeAnalysis? = null,
    val error: String? = null,
    val isLimitReached: Boolean = false,
    val isTooShort: Boolean = false,
)

@HiltViewModel
class ResumeAnalyzerViewModel @Inject constructor(
    private val aiService: AIOpenRouterService,
    private val parser: ResumeAnalysisParser,
    private val rateLimiter: AIRateLimiter,
    private val userPreferences: UserPreferences,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ResumeAnalyzerUiState())
    val uiState: StateFlow<ResumeAnalyzerUiState> = _uiState.asStateFlow()

    fun updateResumeText(text: String) = _uiState.update { it.copy(resumeText = text) }

    fun updateTargetRole(role: String) = _uiState.update { it.copy(targetRole = role) }

    fun analyze() {
        val state = _uiState.value
        val resume = state.resumeText.trim()
        if (state.isLoading || resume.isEmpty()) return

        if (resume.length < MIN_READABLE_CHARS) {
            _uiState.update { it.copy(isTooShort = true, analysis = null, error = null, isLimitReached = false) }
            return
        }

        viewModelScope.launch {
            val isPro = userPreferences.isPro.first()
            if (rateLimiter.canMakeCall(isPro) is RateLimitResult.LimitExceeded) {
                _uiState.update {
                    it.copy(
                        isLimitReached = true,
                        isTooShort = false,
                        analysis = null,
                        error = null,
                    )
                }
                return@launch
            }

            _uiState.update {
                it.copy(
                    isLoading = true,
                    isTooShort = false,
                    isLimitReached = false,
                    analysis = null,
                    error = null,
                )
            }
            try {
                val raw = aiService.sendMessage(
                    listOf(
                        AIOpenRouterService.ChatMessage("system", SCHEMA_PROMPT),
                        AIOpenRouterService.ChatMessage("user", userPrompt(resume, state.targetRole.trim())),
                    ),
                )
                rateLimiter.recordCall()
                val parsed = parser.parse(raw, resume.length)
                if (parsed == null) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = "The analysis came back in a shape we couldn't read, " +
                                "so nothing has been scored.",
                        )
                    }
                } else {
                    _uiState.update { it.copy(isLoading = false, analysis = parsed) }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, error = e.message ?: "The resume could not be analyzed.")
                }
            }
        }
    }

    private fun userPrompt(resume: String, targetRole: String): String {
        val slice = resume.take(MAX_READABLE_CHARS)
        val role = if (targetRole.isEmpty()) {
            "No target role was given — score it against generic entry-level tech hiring."
        } else {
            "Target role: $targetRole."
        }
        return "$role\n\nResume text:\n\"\"\"\n$slice\n\"\"\""
    }
}
