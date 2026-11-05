package com.nexus.growth

import androidx.compose.ui.res.stringResource
import com.nexus.growth.R

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nexus.designsystem.component.EmptyState
import com.nexus.designsystem.component.IntelligenceCard
import com.nexus.designsystem.component.IntelligenceCardData
import com.nexus.designsystem.component.NexusButton
import com.nexus.designsystem.component.NexusCard
import com.nexus.designsystem.component.NexusTopBar
import com.nexus.designsystem.component.feedback.EditorialErrorState
import com.nexus.designsystem.component.feedback.EditorialSkeletonList
import com.nexus.designsystem.component.feedback.EditorialStatusBand
import com.nexus.designsystem.component.feedback.StatusTone
import com.nexus.designsystem.theme.NexusShapes
import com.nexus.designsystem.theme.NexusSpacing
import com.nexus.designsystem.theme.NexusTypography

@Composable
fun NexusOracleScreen(
    onBack: () -> Unit = {},
    viewModel: NexusOracleViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    NexusOracleContent(
        uiState = uiState,
        onBack = onBack,
        onGenerate = viewModel::loadOracle,
    )
}

@Composable
fun NexusOracleContent(
    uiState: OracleUiState,
    onBack: () -> Unit,
    onGenerate: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = NexusSpacing.xl, vertical = NexusSpacing.xs),
        verticalArrangement = Arrangement.spacedBy(NexusSpacing.md),
    ) {
        item {
            NexusTopBar(
                onBack = onBack,
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            stringResource(R.string.growth_nexus_oracle),
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                },
            )
        }

        item {
            val error = uiState.error
            when {
                uiState.isLoading -> EditorialSkeletonList(
                    count = 3,
                    loadingLabel = stringResource(R.string.oracle_reading),
                )

                error != null -> EditorialErrorState(
                    headline = stringResource(R.string.oracle_unable_to_read),
                    reason = stringResource(R.string.oracle_error_reason),
                    actionLabel = stringResource(R.string.oracle_try_again),
                    onAction = onGenerate,
                    secondaryHint = error.takeIf { it.isNotBlank() },
                )

                else -> OracleReading(uiState = uiState, onGenerate = onGenerate)
            }
        }

        item { Spacer(Modifier.height(NexusSpacing.xl)) }
    }
}

/**
 * Nothing on this screen is a measurement of the user's own data — it is model
 * output — so it says so above itself, and shows an honest "not run yet" state
 * instead of the invented figures it used to print unconditionally.
 */
@Composable
private fun OracleReading(
    uiState: OracleUiState,
    onGenerate: () -> Unit,
) {
    if (!uiState.hasGenerated) {
        EmptyState(
            title = stringResource(R.string.oracle_not_connected_title),
            description = stringResource(R.string.oracle_not_connected_body),
            voice = stringResource(R.string.oracle_not_connected_voice),
            actionLabel = stringResource(R.string.oracle_generate),
            onAction = onGenerate,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = NexusSpacing.xxl),
        )
        return
    }

    Column(verticalArrangement = Arrangement.spacedBy(NexusSpacing.md)) {
        EditorialStatusBand(
            label = stringResource(R.string.oracle_estimate_label),
            detail = stringResource(R.string.oracle_estimate_detail),
            tone = StatusTone.Neutral,
        )
        uiState.intelligenceCards.forEach { card ->
            IntelligenceCard(data = card)
        }
        if (uiState.opportunityBody.isNotBlank() || uiState.opportunityTitle.isNotBlank()) {
            OpportunityInsightsCard(
                title = uiState.opportunityTitle.ifBlank {
                    stringResource(R.string.growth_opportunity_intel)
                },
                body = uiState.opportunityBody,
                cta = uiState.opportunityCta,
                onAct = onGenerate,
            )
        }
    }
}

@Composable
private fun OpportunityInsightsCard(
    title: String,
    body: String,
    cta: String,
    onAct: () -> Unit,
) {
    NexusCard(shape = NexusShapes.lg) {
        Text(
            stringResource(R.string.growth_opportunity_intel),
            style = NexusTypography.DataLabel,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(NexusSpacing.xs))
        Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
        if (body.isNotBlank()) {
            Spacer(Modifier.height(NexusSpacing.sm))
            Text(
                body,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
        if (cta.isNotBlank()) {
            Spacer(Modifier.height(NexusSpacing.md))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Timer,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.size(16.dp),
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    cta,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.tertiary,
                )
            }
        }
        Spacer(Modifier.height(NexusSpacing.md))
        NexusButton(text = stringResource(R.string.oracle_regenerate), onClick = onAct)
    }
}
