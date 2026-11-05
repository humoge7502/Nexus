package com.nexus.ai

import androidx.compose.ui.res.stringResource
import com.nexus.ai.R

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
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
import com.nexus.designsystem.component.NexusButton
import com.nexus.designsystem.component.NexusCard
import com.nexus.designsystem.component.NexusTextField
import com.nexus.designsystem.component.NexusTopBar
import com.nexus.designsystem.component.feedback.EditorialErrorState
import com.nexus.designsystem.component.feedback.EditorialSkeletonList
import com.nexus.domain.model.AIMessage
import com.nexus.domain.model.AiMessageRole
import com.nexus.network.ai.AIOpenRouterService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import javax.inject.Inject

sealed interface RoadmapEffect {
    data class ShowXPToast(val amount: Int) : RoadmapEffect
}

@Immutable
data class RoadmapUiState(
    val isLoading: Boolean = false,
    val isLimitReached: Boolean = false,
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
                    // The cap is a state, not an error string: the screen renders
                    // it as the unrecoverable one it is, with the way out on it.
                    _uiState.update {
                        it.copy(isLoading = false, isLimitReached = true, error = null)
                    }
                    return@launch
                }
                is RateLimitResult.Allowed -> Unit
            }

            _uiState.update { it.copy(isLoading = true, isLimitReached = false, error = null) }
            try {
                val prompt = "Create a detailed career roadmap for becoming a $role. Include phases with timeline, skills to learn, projects to build, certifications to get, and resources. Format with clear sections."
                val response = aiService.sendMessage(prompt)
                rateLimiter.recordCall()
                _uiState.update { it.copy(isLoading = false, roadmap = response) }
                analyticsTracker.track("roadmap_generated", mapOf("target_role" to role))

                val result = xpRepository.awardXP(Constants.XP_PER_ROADMAP_MILESTONE, "roadmap_generated")
                _effect.emit(RoadmapEffect.ShowXPToast(result.xpAwarded))
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }
}

@Composable
fun RoadmapGeneratorScreen(
    onBack: () -> Unit = {},
    onGoPro: () -> Unit = {},
    viewModel: RoadmapViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        NexusTopBar(
            title = stringResource(R.string.ai_roadmap_generator),
            onBack = onBack,
        )

        Column(modifier = Modifier.padding(16.dp)) {
            NexusTextField(
                value = uiState.targetRole,
                onValueChange = { viewModel.updateRole(it) },
                label = stringResource(R.string.ai_target_role),
                placeholder = stringResource(R.string.ai_e_g_full_stack_developer_data),
                singleLine = true,
            )
            Spacer(Modifier.height(12.dp))
            NexusButton(
                text = stringResource(
                    if (uiState.isLoading) R.string.ai_generating_button else R.string.ai_generate_roadmap_button,
                ),
                onClick = { viewModel.generate() },
                enabled = uiState.targetRole.isNotBlank() && !uiState.isLoading,
            )
            Spacer(Modifier.height(20.dp))
            RoadmapOutcome(uiState = uiState, onRetry = viewModel::generate, onGoPro = onGoPro)
        }
    }
}

@Composable
private fun RoadmapOutcome(
    uiState: RoadmapUiState,
    onRetry: () -> Unit,
    onGoPro: () -> Unit,
) {
    when {
        uiState.isLoading -> EditorialSkeletonList(
            count = 3,
            loadingLabel = stringResource(R.string.ai_roadmap_loading),
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

        uiState.roadmap.isNotBlank() -> NexusCard(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = uiState.roadmap,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}
