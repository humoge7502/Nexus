package com.nexus.ai

import androidx.compose.ui.res.stringResource
import com.nexus.ai.R

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.*
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
import com.nexus.designsystem.component.LoadingState
import com.nexus.designsystem.component.NexusButton
import com.nexus.designsystem.component.NexusCard
import com.nexus.designsystem.component.NexusChip
import com.nexus.designsystem.component.NexusOutlineButton
import com.nexus.designsystem.component.NexusTextField
import com.nexus.designsystem.component.NexusTopBar
import com.nexus.network.ai.AIOpenRouterService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@Immutable
data class LinkedInPostUiState(
    val isLoading: Boolean = false,
    val achievement: String = "",
    val tone: String = "Professional",
    val generatedPost: String = "",
    val error: String? = null,
    val rateLimited: Boolean = false,
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
        _uiState.value = _uiState.value.copy(achievement = text)
    }

    fun updateTone(tone: String) {
        _uiState.value = _uiState.value.copy(tone = tone)
    }

    fun generate() {
        val achievement = _uiState.value.achievement
        val tone = _uiState.value.tone
        if (achievement.isBlank()) return
        viewModelScope.launch {
            val isPro = userPreferences.isPro.first()
            if (rateLimiter.canMakeCall(isPro) is RateLimitResult.LimitExceeded) {
                _uiState.value = _uiState.value.copy(rateLimited = true, error = "Daily AI limit reached. Upgrade to Pro for unlimited access.")
                return@launch
            }
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val prompt = "Write a LinkedIn post in a $tone tone about this achievement: $achievement. Include relevant hashtags. Keep it under 300 words. Format the post with proper line breaks."
                val response = aiService.sendMessage(prompt)
                rateLimiter.recordCall()
                _uiState.value = _uiState.value.copy(isLoading = false, generatedPost = response)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message ?: "Generation failed")
            }
        }
    }
}

@Composable
fun LinkedInPostScreen(
    onBack: () -> Unit = {},
    viewModel: LinkedInPostViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val clipboard = LocalClipboardManager.current
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
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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
                text = if (uiState.isLoading) "Generating..." else "Generate Post",
                onClick = { viewModel.generate() },
                enabled = uiState.achievement.isNotBlank() && !uiState.isLoading,
            )
            Spacer(Modifier.height(16.dp))

            if (uiState.isLoading) {
                LoadingState()
            }

            uiState.error?.let { error ->
                Text(text = error, color = MaterialTheme.colorScheme.error)
            }

            if (uiState.generatedPost.isNotBlank()) {
                NexusCard(modifier = Modifier.fillMaxWidth()) {
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
    }
}
