package com.nexus.ai

import androidx.compose.ui.res.stringResource
import com.nexus.ai.R

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
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
import com.nexus.designsystem.component.EmptyState
import com.nexus.designsystem.component.NexusCard
import com.nexus.designsystem.component.NexusIconButton
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
data class CareerInsightsUiState(
    val isLoading: Boolean = false,
    val insights: String = "",
    val error: String? = null,
    val isLimitReached: Boolean = false,
)

@HiltViewModel
class CareerInsightsViewModel @Inject constructor(
    private val aiService: AIOpenRouterService,
    private val rateLimiter: AIRateLimiter,
    private val userPreferences: UserPreferences,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CareerInsightsUiState())
    val uiState: StateFlow<CareerInsightsUiState> = _uiState.asStateFlow()

    init {
        loadInsights()
    }

    fun loadInsights() {
        viewModelScope.launch {
            val isPro = userPreferences.isPro.first()
            if (rateLimiter.canMakeCall(isPro) is RateLimitResult.LimitExceeded) {
                _uiState.update { it.copy(isLoading = false, isLimitReached = true, error = null) }
                return@launch
            }
            _uiState.update { it.copy(isLoading = true, error = null, isLimitReached = false) }
            try {
                val currentYear = java.time.Year.now().value
                val prompt = "Provide 5 actionable career insights for CS students in $currentYear. Cover: trending skills, job market, certifications, internships, networking. Numbered sections with headings."
                val response = aiService.sendMessage(prompt)
                rateLimiter.recordCall()
                _uiState.update { it.copy(isLoading = false, insights = response) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }
}

@Composable
fun CareerInsightsScreen(
    onBack: () -> Unit = {},
    onGoPro: () -> Unit,
    viewModel: CareerInsightsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize()) {
        NexusTopBar(
            title = stringResource(R.string.ai_career_insights),
            onBack = onBack,
            actions = {
                NexusIconButton(
                    icon = Icons.Default.Refresh,
                    contentDescription = stringResource(R.string.cd_refresh),
                    onClick = { viewModel.loadInsights() },
                )
            },
        )

        if (uiState.isLoading) {
            EditorialSkeletonList(
                modifier = Modifier.padding(16.dp),
                count = 3,
                loadingLabel = stringResource(R.string.ai_insights_loading),
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
            ) {
                InsightsOutcome(
                    uiState = uiState,
                    onRetry = viewModel::loadInsights,
                    onGoPro = onGoPro,
                )
            }
        }
    }
}

@Composable
private fun InsightsOutcome(
    uiState: CareerInsightsUiState,
    onRetry: () -> Unit,
    onGoPro: () -> Unit,
) {
    when {
        uiState.isLimitReached -> EditorialErrorState(
            headline = stringResource(R.string.ai_daily_limit_headline),
            reason = stringResource(R.string.ai_daily_limit_shared_reason),
            actionLabel = stringResource(R.string.ai_unlock_pro),
            onAction = onGoPro,
            isRecoverable = false,
        )

        uiState.error != null -> EditorialErrorState(
            headline = stringResource(R.string.ai_fault_headline),
            reason = stringResource(R.string.ai_fault_reason),
            actionLabel = stringResource(R.string.ai_retry),
            onAction = onRetry,
            secondaryHint = uiState.error,
        )

        uiState.insights.isNotBlank() -> NexusCard(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = uiState.insights,
                style = MaterialTheme.typography.bodyMedium,
            )
        }

        else -> EmptyState(
            title = stringResource(R.string.ai_insights_empty_title),
            description = stringResource(R.string.ai_insights_empty_body),
            voice = stringResource(R.string.ai_insights_empty_voice),
        )
    }
}
