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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nexus.designsystem.component.CalibratedRule
import com.nexus.designsystem.component.InstrumentGauge
import com.nexus.designsystem.component.MomentumChart
import com.nexus.designsystem.component.ConstellationNode
import com.nexus.designsystem.component.NexusCard
import com.nexus.designsystem.component.SkillConstellation
import com.nexus.designsystem.component.SignalField
import com.nexus.designsystem.component.hairline
import com.nexus.designsystem.component.instrumentGrid
import com.nexus.designsystem.component.sheen
import com.nexus.designsystem.effect.RevealOnLoad
import com.nexus.designsystem.theme.NexusColors
import com.nexus.designsystem.theme.NexusShapes
import com.nexus.designsystem.theme.NexusSpacing
import com.nexus.designsystem.theme.NexusTypography
import java.util.Locale

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
            item(key = "momentum") {
                RevealOnLoad(delayMillis = 110) {
                    MomentumCard(
                        values = uiState.scoreMomentum,
                        weeklyChange = uiState.scoreWeeklyChange,
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
            item(key = "constellation") {
                if (radarAxes.isNotEmpty()) {
                    SkillMapCard(axes = radarAxes)
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
    val shape = NexusShapes.lg
    val bone = NexusColors.Bone

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(
                brush = Brush.linearGradient(listOf(NexusColors.Carbon, NexusColors.VoidBlack)),
                shape = shape,
            )
            .instrumentGrid(spacingDp = 30f, alpha = 0.05f, color = bone)
            .hairline(color = bone.copy(alpha = 0.14f), cornerRadiusDp = 18f)
            .sheen(alpha = 0.06f)
            .padding(horizontal = 22.dp, vertical = 20.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.growth_nexus_score).uppercase(Locale.ROOT),
                        style = NexusTypography.DataLabel,
                        color = bone.copy(alpha = 0.62f),
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = animatedScore.toInt().toString(),
                            style = NexusTypography.TelemetryDisplay.copy(
                                fontSize = 48.sp,
                                lineHeight = 50.sp,
                            ),
                            color = bone,
                        )
                        Text(
                            text = "/ 1000",
                            style = NexusTypography.TelemetryMicro,
                            color = bone.copy(alpha = 0.55f),
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
                    accent = NexusColors.Signal,
                    trackColor = bone.copy(alpha = 0.14f),
                    tickColor = bone.copy(alpha = 0.26f),
                    stroke = 8.dp,
                )
            }

            Spacer(Modifier.height(16.dp))
            CalibratedRule(
                progress = nexusScoreFloat.coerceIn(0f, 1f),
                modifier = Modifier.fillMaxWidth(),
                divisions = 20,
                accent = NexusColors.Signal,
                trackColor = bone.copy(alpha = 0.20f),
                tickColor = bone.copy(alpha = 0.30f),
            )
            Spacer(Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(width = 2.dp, height = 11.dp)
                        .background(NexusColors.Signal)
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    stringResource(
                        if (nexusScore > 0) R.string.growth_live_nexus_score
                        else R.string.growth_start_earning_score,
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = bone.copy(alpha = 0.86f),
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

/**
 * The interactive skill map: the same proficiency data as the radar, drawn as
 * a tappable constellation. Tapping a node reveals its level as real text.
 */
@Composable
private fun SkillMapCard(axes: List<RadarAxis>) {
    var selected by remember { mutableIntStateOf(-1) }
    NexusCard(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.growth_skill_map_title),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 4.dp),
        )
        SkillConstellation(
            nodes = axes.map { ConstellationNode(it.label, it.value) },
            selected = selected.takeIf { it >= 0 },
            onSelect = { selected = if (selected == it) -1 else it },
            modifier = Modifier.fillMaxWidth(),
        )
        val sel = selected.takeIf { it >= 0 }
        if (sel != null && sel < axes.size) {
            val axis = axes[sel]
            Text(
                text = "${axis.label} — ${(axis.value * 100).toInt()}% proficiency",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.height(4.dp))
        } else {
            Text(
                text = stringResource(R.string.growth_skill_map_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

/**
 * The career-momentum instrument: the Ascent score's real daily history from
 * the snapshot table. Empty when no snapshots exist yet — the chart shows its
 * baseline and the caption stays honest about having no readings.
 */
@Composable
private fun MomentumCard(
    values: List<Float>,
    weeklyChange: Int,
) {
    NexusCard(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(R.string.growth_momentum_title),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 12.dp),
        )
        MomentumChart(values = values, modifier = Modifier.fillMaxWidth())
        if (values.isNotEmpty()) {
            Spacer(Modifier.height(10.dp))
            val formatted = String.format(Locale.getDefault(), "%+d", weeklyChange)
            Text(
                text = stringResource(R.string.growth_momentum_caption, formatted),
                style = MaterialTheme.typography.bodySmall,
                color = if (weeklyChange >= 0) {
                    NexusColors.SignalDeep
                } else {
                    MaterialTheme.colorScheme.error
                },
            )
        }
    }
}

