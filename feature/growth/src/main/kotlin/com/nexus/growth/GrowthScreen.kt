package com.nexus.growth

import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import com.nexus.growth.R

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nexus.designsystem.component.InstrumentGauge
import com.nexus.designsystem.component.LeaderRule
import com.nexus.designsystem.component.SignalField
import com.nexus.designsystem.component.hairline
import com.nexus.designsystem.component.instrumentGrid
import com.nexus.designsystem.component.sheen
import com.nexus.designsystem.effect.RevealOnLoad
import com.nexus.designsystem.theme.NexusColors
import com.nexus.designsystem.theme.NexusGradients
import com.nexus.designsystem.theme.NexusSpacing
import com.nexus.designsystem.theme.NexusTypography

internal data class Metric(val label: String, val progress: Float)

internal data class RadarAxis(val label: String, val value: Float)

@Composable
fun GrowthScreen(
    onDsaTracker: () -> Unit = {},
    onGoals: () -> Unit = {},
    onOracle: () -> Unit = {},
    onArena: () -> Unit = {},
    onBack: () -> Unit = {},
    showBackArrow: Boolean = true,
    viewModel: GrowthViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val metrics = listOf(
        Metric("Skill progress", uiState.skillProgress),
        Metric("Opportunity engagement", uiState.opportunityEngagement),
        Metric("Consistency", uiState.consistency),
        Metric("Portfolio", uiState.portfolioStrength),
        Metric("Achievements", uiState.achievementsProgress),
    )
    val radarAxes = uiState.skillAxes.map { RadarAxis(it.label, it.value) }
    val goals = (uiState.goals as? com.nexus.domain.model.UiState.Success)?.data.orEmpty()

    Box(modifier = Modifier.fillMaxSize()) {
        SignalField(modifier = Modifier.fillMaxSize())

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = 12.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(NexusSpacing.lg),
        ) {
            item(key = "header") {
                GrowthHeader(showBackArrow = showBackArrow, onBack = onBack)
            }
            item(key = "score_card") {
                RevealOnLoad(delayMillis = 40) {
                    NexusScoreCard(
                        nexusScore = uiState.nexusScore,
                        nexusScoreFloat = uiState.nexusScoreFloat,
                        metrics = metrics,
                    )
                }
            }
            item(key = "radar") {
                if (radarAxes.isEmpty()) {
                    SkillRadarEmpty()
                } else {
                    SkillRadarChart(radarAxes)
                }
            }
            item(key = "streak") {
                LearningStreak(
                    currentStreak = uiState.currentStreak,
                    longestStreak = uiState.longestStreak,
                    thisMonthActive = uiState.thisMonthActive,
                    weekActivity = uiState.weekActivity,
                )
            }
            item(key = "stats") {
                QuickStatsRow(
                    problemsSolved = uiState.dsaSolved,
                    goalsCompleted = uiState.completedGoals,
                    projects = uiState.projectsCount,
                )
            }
            item(key = "goals") { GoalsPreview(goals = goals, onGoals = onGoals) }
            item(key = "nexus_actions") { NexusActionsRow(onOracle = onOracle, onArena = onArena) }
            item(key = "challenge") {
                TodaysChallenge(challenge = uiState.dailyChallenge, onStart = onDsaTracker)
            }
        }
    }
}

@Composable
private fun GrowthHeader(showBackArrow: Boolean, onBack: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (showBackArrow) {
            IconButton(onClick = onBack, modifier = Modifier.padding(end = 4.dp).size(32.dp)) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.cd_back),
                    modifier = Modifier.size(20.dp),
                )
            }
        }
        Text(
            text = stringResource(R.string.growth_growth),
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground,
        )
    }
}

@Composable
private fun NexusScoreCard(nexusScore: Int, nexusScoreFloat: Float, metrics: List<Metric>) {
    val animatedScore by animateFloatAsState(
        targetValue = nexusScore.toFloat().coerceIn(0f, 1000f),
        animationSpec = tween(1400, easing = FastOutSlowInEasing),
        label = "score",
    )
    val shape = RoundedCornerShape(28.dp)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(brush = Brush.linearGradient(NexusGradients.PrimaryDeep), shape = shape)
            .instrumentGrid(spacingDp = 30f, alpha = 0.045f)
            .hairline(color = Color.White.copy(alpha = 0.16f), cornerRadiusDp = 28f)
            .sheen(alpha = 0.07f)
            .padding(horizontal = 22.dp, vertical = 20.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.growth_nexus_score),
                        style = MaterialTheme.typography.titleSmall,
                        color = Color.White.copy(alpha = 0.70f),
                    )
                    Spacer(Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = animatedScore.toInt().toString(),
                            style = NexusTypography.TelemetryDisplay.copy(
                                fontSize = 48.sp,
                                lineHeight = 50.sp,
                            ),
                            color = Color.White,
                        )
                        Text(
                            text = "/ 1000",
                            style = NexusTypography.TelemetryMicro,
                            color = Color.White.copy(alpha = 0.62f),
                            modifier = Modifier.padding(start = 6.dp, bottom = 8.dp),
                        )
                    }
                }
                Spacer(Modifier.width(12.dp))
                InstrumentGauge(
                    progress = nexusScoreFloat.coerceIn(0f, 1f),
                    modifier = Modifier
                        .size(120.dp)
                        .semantics {
                            progressBarRangeInfo = ProgressBarRangeInfo(
                                nexusScore.toFloat().coerceIn(0f, 1000f),
                                0f..1000f,
                            )
                        },
                    accent = NexusColors.Cyan400,
                    trackColor = Color.White.copy(alpha = 0.15f),
                    tickColor = Color.White.copy(alpha = 0.32f),
                    stroke = 8.dp,
                )
            }

            Spacer(Modifier.height(16.dp))
            HorizontalDivider(thickness = 1.dp, color = Color.White.copy(alpha = 0.14f))
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(5.dp)
                        .clip(CircleShape)
                        .background(NexusColors.Cyan400)
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    stringResource(
                        if (nexusScore > 0) R.string.growth_live_nexus_score
                        else R.string.growth_start_earning_score,
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.92f),
                )
            }

            Spacer(Modifier.height(16.dp))
            for (metric in metrics) {
                MetricBar(metric)
                Spacer(Modifier.height(6.dp))
            }
        }
    }
}

