package com.nexus.ai

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nexus.common.AIRateLimiter
import com.nexus.common.RateLimitResult
import com.nexus.datastore.UserPreferences
import com.nexus.designsystem.component.LoadingState
import com.nexus.designsystem.component.NexusCard
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LinkedInPostScreen(
    onBack: () -> Unit = {},
    viewModel: LinkedInPostViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val clipboard = LocalClipboardManager.current
    val tones = listOf("Professional", "Casual", "Inspiring", "Humorous", "Thankful")

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("LinkedIn Post Generator", fontWeight = FontWeight.Bold) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface),
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        ) {
            OutlinedTextField(
                value = uiState.achievement,
                onValueChange = { viewModel.updateAchievement(it) },
                label = { Text("Describe your achievement") },
                placeholder = { Text("e.g., Won a hackathon, completed a certification, built a project") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 6,
            )
            Spacer(Modifier.height(12.dp))
            Text("Tone:", style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                tones.forEach { tone ->
                    FilterChip(
                        selected = uiState.tone == tone,
                        onClick = { viewModel.updateTone(tone) },
                        label = { Text(tone) },
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = { viewModel.generate() },
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState.achievement.isNotBlank() && !uiState.isLoading,
            ) {
                Text(if (uiState.isLoading) "Generating..." else "Generate Post")
            }
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
                    OutlinedButton(
                        onClick = { clipboard.setText(AnnotatedString(uiState.generatedPost)) },
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Copy to Clipboard")
                    }
                }
            }
        }
    }
}
