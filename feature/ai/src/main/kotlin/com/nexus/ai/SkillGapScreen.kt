package com.nexus.ai

import androidx.compose.ui.res.stringResource
import com.nexus.ai.R

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
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
data class SkillGapUiState(
    val isLoading: Boolean = false,
    val currentSkills: String = "",
    val targetRole: String = "",
    val analysis: String = "",
    val error: String? = null,
    val isLimitReached: Boolean = false,
)

@HiltViewModel
class SkillGapViewModel @Inject constructor(
    private val aiService: AIOpenRouterService,
    private val rateLimiter: AIRateLimiter,
    private val userPreferences: UserPreferences,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SkillGapUiState())
    val uiState: StateFlow<SkillGapUiState> = _uiState.asStateFlow()

    fun updateCurrentSkills(skills: String) {
        _uiState.update { it.copy(currentSkills = skills) }
    }

    fun updateTargetRole(role: String) {
        _uiState.update { it.copy(targetRole = role) }
    }

    fun analyze() {
        val skills = _uiState.value.currentSkills
        val role = _uiState.value.targetRole
        if (skills.isBlank() || role.isBlank()) return
        viewModelScope.launch {
            val isPro = userPreferences.isPro.first()
            if (rateLimiter.canMakeCall(isPro) is RateLimitResult.LimitExceeded) {
                _uiState.update { it.copy(isLimitReached = true, error = null) }
                return@launch
            }
            _uiState.update { it.copy(isLoading = true, error = null, isLimitReached = false) }
            try {
                val prompt = "Analyze the skill gap between current skills ($skills) and requirements for a $role. List: 1. Skills I already have. 2. Skills I need to learn (prioritized). 3. Recommended resources. 4. Estimated timeline."
                val response = aiService.sendMessage(prompt)
                rateLimiter.recordCall()
                _uiState.update { it.copy(isLoading = false, analysis = response) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }
}

@Composable
fun SkillGapScreen(
    onBack: () -> Unit = {},
    onGoPro: () -> Unit,
    viewModel: SkillGapViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize()) {
        NexusTopBar(
            title = stringResource(R.string.ai_skill_gap_analysis),
            onBack = onBack,
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
        ) {
            NexusTextField(
                value = uiState.currentSkills,
                onValueChange = { viewModel.updateCurrentSkills(it) },
                label = stringResource(R.string.ai_your_current_skills),
                placeholder = stringResource(R.string.ai_e_g_python_java_html_css),
                singleLine = false,
                minLines = 2,
            )
            Spacer(Modifier.height(12.dp))
            NexusTextField(
                value = uiState.targetRole,
                onValueChange = { viewModel.updateTargetRole(it) },
                label = stringResource(R.string.ai_target_role),
                placeholder = stringResource(R.string.ai_e_g_machine_learning_engineer_full),
                singleLine = true,
            )
            Spacer(Modifier.height(16.dp))
            NexusButton(
                text = stringResource(R.string.ai_analyze_gap_button),
                onClick = { viewModel.analyze() },
                loading = uiState.isLoading,
                settled = uiState.analysis.isNotBlank() && !uiState.isLoading,
                settledText = stringResource(R.string.ai_analysis_complete),
                enabled = uiState.currentSkills.isNotBlank() &&
                    uiState.targetRole.isNotBlank() &&
                    !uiState.isLoading,
            )
            if (uiState.analysis.isNotBlank()) {
                Spacer(Modifier.height(16.dp))
            }
            SkillGapOutcome(uiState = uiState, onRetry = viewModel::analyze, onGoPro = onGoPro)
        }
    }
}

@Composable
private fun SkillGapOutcome(
    uiState: SkillGapUiState,
    onRetry: () -> Unit,
    onGoPro: () -> Unit,
) {
    when {
        uiState.isLoading -> EditorialSkeletonList(
            count = 2,
            loadingLabel = stringResource(R.string.ai_analyzing_gap_loading),
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

        uiState.analysis.isNotBlank() -> NexusCard(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = uiState.analysis,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}
