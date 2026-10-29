package com.nexus.ai

import androidx.compose.ui.res.stringResource
import com.nexus.ai.R

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
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
import com.nexus.designsystem.component.LoadingState
import com.nexus.designsystem.component.NexusCard
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
data class SkillGapUiState(
    val isLoading: Boolean = false,
    val currentSkills: String = "",
    val targetRole: String = "",
    val analysis: String = "",
    val error: String? = null,
    val rateLimited: Boolean = false,
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
        _uiState.value = _uiState.value.copy(currentSkills = skills)
    }

    fun updateTargetRole(role: String) {
        _uiState.value = _uiState.value.copy(targetRole = role)
    }

    fun analyze() {
        val skills = _uiState.value.currentSkills
        val role = _uiState.value.targetRole
        if (skills.isBlank() || role.isBlank()) return
        viewModelScope.launch {
            val isPro = userPreferences.isPro.first()
            if (rateLimiter.canMakeCall(isPro) is RateLimitResult.LimitExceeded) {
                _uiState.value = _uiState.value.copy(rateLimited = true, error = "Daily AI limit reached. Upgrade to Pro for unlimited access.")
                return@launch
            }
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                val prompt = "Analyze the skill gap between current skills ($skills) and requirements for a $role. List: 1. Skills I already have. 2. Skills I need to learn (prioritized). 3. Recommended resources. 4. Estimated timeline."
                val response = aiService.sendMessage(prompt)
                rateLimiter.recordCall()
                _uiState.value = _uiState.value.copy(isLoading = false, analysis = response)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false, error = e.message ?: "Analysis failed")
            }
        }
    }
}

@Composable
fun SkillGapScreen(
    onBack: () -> Unit = {},
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
            OutlinedTextField(
                value = uiState.currentSkills,
                onValueChange = { viewModel.updateCurrentSkills(it) },
                label = { Text(stringResource(R.string.ai_your_current_skills)) },
                placeholder = { Text(stringResource(R.string.ai_e_g_python_java_html_css)) },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                maxLines = 4,
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = uiState.targetRole,
                onValueChange = { viewModel.updateTargetRole(it) },
                label = { Text(stringResource(R.string.ai_target_role)) },
                placeholder = { Text(stringResource(R.string.ai_e_g_machine_learning_engineer_full)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = { viewModel.analyze() },
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState.currentSkills.isNotBlank() && uiState.targetRole.isNotBlank() && !uiState.isLoading,
            ) {
                Text(if (uiState.isLoading) "Analyzing..." else "Analyze Gap")
            }
            Spacer(Modifier.height(16.dp))

            if (uiState.isLoading) {
                LoadingState()
            }

            uiState.error?.let { error ->
                Text(text = error, color = MaterialTheme.colorScheme.error)
            }

            if (uiState.analysis.isNotBlank()) {
                NexusCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = uiState.analysis,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }
    }
}
