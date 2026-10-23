package com.nexus.ai

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nexus.analytics.AnalyticsTracker
import com.nexus.common.AIRateLimiter
import com.nexus.common.ApiResult
import com.nexus.common.Constants
import com.nexus.common.RateLimitResult
import com.nexus.data.repository.UserXpRepository
import com.nexus.designsystem.component.LoadingState
import com.nexus.designsystem.component.NexusCard
import com.nexus.domain.model.AIMessage
import com.nexus.domain.model.AiMessageRole
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.Instant
import javax.inject.Inject

sealed interface RoadmapEffect {
    data class ShowXPToast(val amount: Int) : RoadmapEffect
}

@Immutable
data class RoadmapUiState(
    val isLoading: Boolean = false,
    val targetRole: String = "",
    val roadmap: String = "",
    val error: String? = null,
)

@HiltViewModel
class RoadmapViewModel @Inject constructor(
    private val aiService: AIOpenRouterService,
    private val rateLimiter: AIRateLimiter,
    private val xpRepository: UserXpRepository,
    private val analyticsTracker: AnalyticsTracker,
    private val userPreferences: com.nexus.datastore.UserPreferences,
) : ViewModel() {

    private val _uiState = MutableStateFlow(RoadmapUiState())
    val uiState: StateFlow<RoadmapUiState> = _uiState.asStateFlow()

    private val _effect = MutableSharedFlow<RoadmapEffect>()
    val effect: SharedFlow<RoadmapEffect> = _effect.asSharedFlow()

    init {
        analyticsTracker.track("screen_view", mapOf("screen" to "roadmap_generator"))
    }

    fun updateRole(role: String) {
        _uiState.value = _uiState.value.copy(targetRole = role)
    }

    fun generate() {
        val role = _uiState.value.targetRole
        if (role.isBlank()) return
viewModelScope.launch {
        val isPro = userPreferences.isPro.first()
        when (val limitResult = rateLimiter.canMakeCall(isPro)) {
                is RateLimitResult.LimitExceeded -> {
                    _uiState.value = _uiState.value.copy(error = "Daily AI limit reached. Upgrade to Pro for unlimited access.")
                    return@launch
                }
                is RateLimitResult.Allowed -> Unit
            }

            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val prompt = "Create a detailed career roadmap for becoming a $role. Include phases with timeline, skills to learn, projects to build, certifications to get, and resources. Format with clear sections."
                val response = aiService.sendMessage(prompt)
                rateLimiter.recordCall()
                _uiState.value = _uiState.value.copy(isLoading = false, roadmap = response)
                analyticsTracker.track("roadmap_generated", mapOf("target_role" to role))

                val result = xpRepository.awardXP(Constants.XP_PER_ROADMAP_MILESTONE, "roadmap_generated")
                _effect.emit(RoadmapEffect.ShowXPToast(result.xpAwarded))
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message ?: "Generation failed")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoadmapGeneratorScreen(
    onBack: () -> Unit = {},
    viewModel: RoadmapViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Roadmap Generator", fontWeight = FontWeight.Bold) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface),
        )

        Column(modifier = Modifier.padding(16.dp)) {
            OutlinedTextField(
                value = uiState.targetRole,
                onValueChange = { viewModel.updateRole(it) },
                label = { Text("Target Role") },
                placeholder = { Text("e.g., Full Stack Developer, Data Scientist") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = { viewModel.generate() },
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState.targetRole.isNotBlank() && !uiState.isLoading,
            ) {
                Text(if (uiState.isLoading) "Generating..." else "Generate Roadmap")
            }
        }

        if (uiState.isLoading) {
            LoadingState(modifier = Modifier.fillMaxSize())
        }

        uiState.error?.let { error ->
            Text(
                text = error, color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(16.dp),
            )
        }

        if (uiState.roadmap.isNotBlank()) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
            ) {
                item {
                    NexusCard(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = uiState.roadmap,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }
        }
    }
}
