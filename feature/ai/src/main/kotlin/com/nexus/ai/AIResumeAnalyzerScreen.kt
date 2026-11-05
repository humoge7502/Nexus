package com.nexus.ai

import androidx.compose.ui.res.stringResource
import com.nexus.ai.R

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nexus.designsystem.component.EmptyState
import com.nexus.designsystem.component.NexusButton
import com.nexus.designsystem.component.NexusCard
import com.nexus.designsystem.component.NexusTextField
import com.nexus.designsystem.component.NexusTopBar
import com.nexus.designsystem.component.SignalTag
import com.nexus.designsystem.component.SignalTone
import com.nexus.designsystem.component.feedback.EditorialErrorState
import com.nexus.designsystem.component.feedback.EditorialSkeletonList
import com.nexus.designsystem.theme.NexusColors
import com.nexus.designsystem.theme.NexusGradients
import com.nexus.designsystem.theme.NexusShapes
import com.nexus.designsystem.theme.NexusTypography
import com.nexus.designsystem.theme.nexus
import java.util.Locale

@Composable
fun AIResumeAnalyzerScreen(
    onBack: () -> Unit = {},
    onGoPro: () -> Unit,
    viewModel: ResumeAnalyzerViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
    ) {
        NexusTopBar(
            title = stringResource(R.string.ai_resume_analyzer),
            onBack = onBack,
        )

        Column(modifier = Modifier.padding(16.dp)) {
            ResumeInputs(
                resumeText = uiState.resumeText,
                targetRole = uiState.targetRole,
                isBusy = uiState.isLoading,
                onResumeChange = viewModel::updateResumeText,
                onRoleChange = viewModel::updateTargetRole,
                onAnalyze = viewModel::analyze,
            )
            Spacer(Modifier.height(20.dp))
            ResumeOutcome(uiState = uiState, onRetry = viewModel::analyze, onGoPro = onGoPro)
        }
    }
}

@Composable
private fun ResumeInputs(
    resumeText: String,
    targetRole: String,
    isBusy: Boolean,
    onResumeChange: (String) -> Unit,
    onRoleChange: (String) -> Unit,
    onAnalyze: () -> Unit,
) {
    NexusCard(modifier = Modifier.fillMaxWidth(), shape = NexusShapes.lg) {
        Column {
            NexusTextField(
                value = resumeText,
                onValueChange = onResumeChange,
                label = stringResource(R.string.ai_resume_text_label),
                placeholder = stringResource(R.string.ai_resume_text_placeholder),
                supportingText = stringResource(R.string.ai_resume_supporting),
                singleLine = false,
                minLines = 8,
            )
            Spacer(Modifier.height(12.dp))
            NexusTextField(
                value = targetRole,
                onValueChange = onRoleChange,
                label = stringResource(R.string.ai_target_role_optional),
                placeholder = stringResource(R.string.ai_e_g_machine_learning_engineer_full),
                singleLine = true,
            )
            Spacer(Modifier.height(16.dp))
            NexusButton(
                text = stringResource(
                    if (isBusy) R.string.ai_analyzing_button else R.string.ai_analyze_button,
                ),
                onClick = onAnalyze,
                enabled = resumeText.isNotBlank() && !isBusy,
            )
        }
    }
}

@Composable
private fun ResumeOutcome(
    uiState: ResumeAnalyzerUiState,
    onRetry: () -> Unit,
    onGoPro: () -> Unit,
) {
    when {
        uiState.isLoading -> EditorialSkeletonList(
            count = 3,
            loadingLabel = stringResource(R.string.ai_analyzing_resume),
        )

        uiState.isLimitReached -> EditorialErrorState(
            headline = stringResource(R.string.ai_daily_limit_headline),
            reason = stringResource(R.string.ai_daily_limit_reason),
            actionLabel = stringResource(R.string.ai_unlock_pro),
            onAction = onGoPro,
            isRecoverable = false,
        )

        uiState.error != null -> EditorialErrorState(
            headline = stringResource(R.string.ai_could_not_analyze),
            reason = stringResource(R.string.ai_could_not_analyze_reason),
            actionLabel = stringResource(R.string.ai_retry),
            onAction = onRetry,
            secondaryHint = uiState.error,
        )

        uiState.isTooShort -> EmptyState(
            title = stringResource(R.string.ai_too_short_title),
            description = stringResource(R.string.ai_too_short_body),
            voice = stringResource(R.string.ai_too_short_voice),
        )

        uiState.analysis == null -> EmptyState(
            title = stringResource(R.string.ai_paste_resume_title),
            description = stringResource(R.string.ai_paste_resume_body),
            voice = stringResource(R.string.ai_paste_resume_voice),
        )

        else -> ResumeResult(uiState.analysis)
    }
}

@Composable
private fun ResumeResult(analysis: ResumeAnalysis) {
    Column {
        ScoreCard(analysis)
        Spacer(Modifier.height(24.dp))
        Text(
            stringResource(R.string.ai_detailed_analysis),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(Modifier.height(12.dp))
        analysis.sections.forEach { SectionRow(it) }

        if (analysis.missingKeywords.isNotEmpty()) {
            Spacer(Modifier.height(24.dp))
            Heading(R.string.ai_missing_keywords_heading)
            Spacer(Modifier.height(10.dp))
            KeywordRow(analysis.missingKeywords)
        }

        if (analysis.recommendations.isNotEmpty()) {
            Spacer(Modifier.height(24.dp))
            Heading(R.string.ai_recommendations_heading)
            Spacer(Modifier.height(10.dp))
            RecommendationList(analysis.recommendations)
        }
    }
}

@Composable
private fun Heading(textResId: Int) {
    Text(
        stringResource(textResId).uppercase(Locale.ROOT),
        style = NexusTypography.DataLabel,
        color = MaterialTheme.nexus.signal,
    )
}

@Composable
private fun ScoreCard(analysis: ResumeAnalysis) {
    NexusCard(gradient = NexusGradients.Primary, shape = NexusShapes.lg) {
        Column {
            Text(
                stringResource(R.string.ai_ats_score).uppercase(Locale.ROOT),
                style = NexusTypography.DataLabel,
                color = NexusColors.SignalInk.copy(alpha = 0.86f),
            )
            Spacer(Modifier.height(10.dp))
            Text(
                analysis.atsScore.toString(),
                style = NexusTypography.TelemetryDisplay,
                color = NexusColors.SignalInk,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                verdictFor(analysis.atsScore),
                style = MaterialTheme.typography.bodyMedium,
                color = NexusColors.SignalInk.copy(alpha = 0.86f),
            )
            if (analysis.charactersRead < analysis.charactersGiven) {
                Spacer(Modifier.height(8.dp))
                Text(
                    stringResource(
                        R.string.ai_scored_from_prefix,
                        analysis.charactersRead,
                        analysis.charactersGiven,
                    ),
                    style = MaterialTheme.typography.labelSmall,
                    color = NexusColors.SignalInk.copy(alpha = 0.78f),
                )
            }
        }
    }
}

@Composable
private fun SectionRow(section: ResumeSectionScore) {
    val ext = MaterialTheme.nexus
    val toneColor = when {
        section.score >= 80 -> ext.positive
        section.score >= 60 -> MaterialTheme.colorScheme.primary
        section.score >= 40 -> ext.attention
        else -> MaterialTheme.colorScheme.error
    }
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                section.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
            )
            Text(
                stringResource(R.string.ai_score_percent, section.score),
                style = MaterialTheme.typography.labelSmall,
                color = toneColor,
                fontWeight = FontWeight.Bold,
            )
        }
        Spacer(Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { section.score / 100f },
            modifier = Modifier.fillMaxWidth().height(4.dp).clip(NexusShapes.tag),
            color = toneColor,
            trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
        )
        if (section.note.isNotEmpty()) {
            Spacer(Modifier.height(4.dp))
            Text(
                section.note,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun KeywordRow(keywords: List<String>) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        keywords.forEach { keyword ->
            SignalTag(text = keyword, tone = SignalTone.Attention)
        }
    }
}

@Composable
private fun RecommendationList(recommendations: List<String>) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        recommendations.forEachIndexed { index, recommendation ->
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    (index + 1).toString(),
                    style = NexusTypography.DataLabel,
                    color = MaterialTheme.nexus.signal,
                )
                Text(
                    recommendation,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun verdictFor(score: Int): String = stringResource(
    when {
        score >= 85 -> R.string.ai_verdict_strong
        score >= 70 -> R.string.ai_verdict_competitive
        score >= 50 -> R.string.ai_verdict_gaps
        else -> R.string.ai_verdict_needs_work
    },
)
