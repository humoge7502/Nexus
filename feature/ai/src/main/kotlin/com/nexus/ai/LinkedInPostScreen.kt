package com.nexus.ai

import androidx.compose.ui.res.stringResource
import com.nexus.ai.R

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nexus.common.AIRateLimiter
import com.nexus.common.RateLimitResult
import com.nexus.datastore.UserPreferences
import com.nexus.designsystem.component.NexusButton
import com.nexus.designsystem.component.NexusCard
import com.nexus.designsystem.component.NexusChip
import com.nexus.designsystem.component.NexusOutlineButton
import com.nexus.designsystem.component.NexusTextField
import com.nexus.designsystem.component.NexusTopBar
import com.nexus.designsystem.component.feedback.EditorialErrorState
import com.nexus.designsystem.component.feedback.EditorialSkeletonList
import com.nexus.network.ai.AIOpenRouterService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@Immutable
data class LinkedInPostUiState(
    val isLoading: Boolean = false,
    val achievement: String = "",
    val tone: String = "Professional",
    val generatedPost: String = "",
    val error: String? = null,
    val isLimitReached: Boolean = false,
)

@HiltViewModel
class LinkedInPostViewModel @Inject constructor(
    private val aiService: AIOpenRouterService,
    private val rateLimiter: AIRateLimiter,
    private val userPreferences: UserPreferences,
) : ViewModel() {

    private val _uiState = MutableStateFlow(LinkedInPostUiState())
    val uiState: StateFlow<LinkedInPostUiState> = _uiState.asStateFlow()

    fun updateAchievement(text: String) {
        _uiState.update { it.copy(achievement = text) }
    }

    fun updateTone(tone: String) {
        _uiState.update { it.copy(tone = tone) }
    }

    fun generate() {
        val achievement = _uiState.value.achievement
        val tone = _uiState.value.tone
        if (achievement.isBlank()) return
        viewModelScope.launch {
            val isPro = userPreferences.isPro.first()
            if (rateLimiter.canMakeCall(isPro) is RateLimitResult.LimitExceeded) {
                _uiState.update { it.copy(isLimitReached = true, error = null) }
                return@launch
            }
            _uiState.update { it.copy(isLoading = true, error = null, isLimitReached = false) }
            try {
                val prompt = "Write a LinkedIn post in a $tone tone about this achievement: $achievement. Include relevant hashtags. Keep it under 300 words. Format the post with proper line breaks."
                val response = aiService.sendMessage(prompt)
                rateLimiter.recordCall()
                _uiState.update { it.copy(isLoading = false, generatedPost = response) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }
}

@Composable
@OptIn(ExperimentalLayoutApi::class)
fun LinkedInPostScreen(
    onBack: () -> Unit = {},
    onGoPro: () -> Unit,
    viewModel: LinkedInPostViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val tones = listOf("Professional", "Casual", "Inspiring", "Humorous", "Thankful")

    Column(modifier = Modifier.fillMaxSize()) {
        NexusTopBar(
            title = stringResource(R.string.ai_linkedin_post_generator),
            onBack = onBack,
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        ) {
            NexusTextField(
                value = uiState.achievement,
                onValueChange = { viewModel.updateAchievement(it) },
                label = stringResource(R.string.ai_describe_your_achievement),
                placeholder = stringResource(R.string.ai_e_g_won_a_hackathon_completed),
                singleLine = false,
                minLines = 2,
            )
            Spacer(Modifier.height(12.dp))
            Text(stringResource(R.string.ai_tone), style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.height(8.dp))
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                tones.forEach { tone ->
                    NexusChip(
                        text = tone,
                        isSelected = uiState.tone == tone,
                        onToggle = { viewModel.updateTone(tone) },
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
            NexusButton(
                text = stringResource(
                    if (uiState.isLoading) R.string.ai_generating_button else R.string.ai_generate_post_button,
                ),
                onClick = { viewModel.generate() },
                enabled = uiState.achievement.isNotBlank() && !uiState.isLoading,
            )
            if (uiState.generatedPost.isNotBlank()) {
                Spacer(Modifier.height(16.dp))
            }
            PostOutcome(uiState = uiState, onRetry = viewModel::generate, onGoPro = onGoPro)
        }
    }
}

@Composable
private fun PostOutcome(
    uiState: LinkedInPostUiState,
    onRetry: () -> Unit,
    onGoPro: () -> Unit,
) {
    val clipboard = LocalClipboardManager.current
    when {
        uiState.isLoading -> EditorialSkeletonList(
            count = 2,
            loadingLabel = stringResource(R.string.ai_drafting_post_loading),
        )

        uiState.isLimitReached -> EditorialErrorState(
            headline = stringResource(R.string.ai_daily_limit_headline),
            reason = stringResource(R.string.ai_daily_limit_shared_reason),
            actionLabel = stringResource(R.string.ai_unlock_pro),
            onAction = onGoPro,
            isRecoverable = false,
        )

        uiState.error != null -> EditorialErrorState(
            headline = stringResource(R.string.ai_fault_headline),
            reason = stringResource(R.string.ai_fault_reason_inputs),
            actionLabel = stringResource(R.string.ai_retry),
            onAction = onRetry,
            secondaryHint = uiState.error,
        )

        uiState.generatedPost.isNotBlank() -> NexusCard(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = uiState.generatedPost,
                style = MaterialTheme.typography.bodyMedium,
            )
            Spacer(Modifier.height(12.dp))
            NexusOutlineButton(
                text = stringResource(R.string.ai_copy_to_clipboard),
                onClick = { clipboard.setText(AnnotatedString(uiState.generatedPost)) },
                icon = Icons.Default.ContentCopy,
                expand = false,
            )
        }
    }
}
