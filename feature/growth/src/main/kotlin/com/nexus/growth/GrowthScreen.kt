package com.nexus.growth

import androidx.compose.ui.res.stringResource
import com.nexus.growth.R

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nexus.common.Constants
import com.nexus.designsystem.component.InstrumentPlate
import com.nexus.designsystem.component.MomentumChart
import com.nexus.designsystem.component.ConstellationNode
import com.nexus.designsystem.component.NexusIconButton
import com.nexus.designsystem.component.NexusCard
import com.nexus.designsystem.component.SkillConstellation
import com.nexus.designsystem.component.SignalField
import com.nexus.designsystem.component.feedback.EditorialErrorState
import com.nexus.designsystem.component.feedback.EditorialSkeletonList
import com.nexus.designsystem.component.editorial.CareerProgressionReveal
import com.nexus.designsystem.component.editorial.ProgressionReading
import com.nexus.designsystem.component.intelligence.IntelligenceInsight
import com.nexus.designsystem.effect.RevealOnLoad
import com.nexus.designsystem.theme.NexusColors
import com.nexus.designsystem.theme.NexusSpacing
import com.nexus.designsystem.theme.NexusTypography
import java.util.Locale

internal data class RadarAxis(val label: String, val value: Float)

@Composable
fun GrowthScreen(
    onDsaTracker: () -> Unit = {},
    onGoals: () -> Unit = {},
    onOracle: () -> Unit = {},
    onArena: () -> Unit = {},
    onSkills: () -> Unit = {},
    onBack: () -> Unit = {},
    showBackArrow: Boolean = true,
    viewModel: GrowthViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val readings = uiState.scoreComponents.map { component ->
        ProgressionReading(
            label = componentLabel(component.key),
            earned = component.earned,
            available = component.available,
        )
    }
    // A single snapshot cannot show a change; reporting 0 would dress an unmeasured
    // quantity up as a measurement.
    val weeklyChange = uiState.scoreWeeklyChange
        .takeIf { uiState.scoreMomentum.size >= 2 && uiState.scoreComponents.isNotEmpty() }
    // No cohort data yet, so the rank line stays absent rather than implied.
    val percentile: Int? = null
    val radarAxes = uiState.skillAxes.map { RadarAxis(it.label, it.value) }
    val goals = when (val s = uiState.goals) {
        is com.nexus.domain.model.UiState.Success -> s.data
        else -> emptyList()
    }
    val goalsFault = (uiState.goals as? com.nexus.domain.model.UiState.Error)?.message
    val metricsFault = uiState.metricsFault
    // Every figure below is derived from the metrics pipeline. While it is in
    // flight or has failed, those numbers are defaults — so they stay off
    // screen instead of reporting a score of 0 as if it were a measurement.
    val metricsRead = uiState.metricsLoaded && metricsFault == null

    Box(modifier = Modifier.fillMaxSize()) {
        SignalField(modifier = Modifier.fillMaxSize())

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = 12.dp, bottom = 116.dp),
            verticalArrangement = Arrangement.spacedBy(NexusSpacing.lg),
        ) {
            item(key = "header") {
                GrowthHeader(showBackArrow = showBackArrow, onBack = onBack)
            }
            item(key = "score_card") {
                when {
                    metricsFault != null -> RevealOnLoad(delayMillis = 40) {
                        EditorialErrorState(
                            headline = stringResource(R.string.growth_unable_to_measure),
                            reason = stringResource(R.string.growth_metrics_error_reason),
                            actionLabel = stringResource(R.string.growth_try_again),
                            onAction = viewModel::retryMetrics,
                            secondaryHint = metricsFault.takeIf { it.isNotBlank() },
                        )
                    }

                    !metricsRead -> RevealOnLoad(delayMillis = 40) {
                        EditorialSkeletonList(
                            count = 1,
                            loadingLabel = stringResource(R.string.growth_loading_metrics),
                        )
                    }

                    else -> RevealOnLoad(delayMillis = 40) {
                        ScoreLedger(
                            score = uiState.nexusScore,
                            weeklyChange = weeklyChange,
                            percentile = percentile,
                            readings = readings,
                        )
                    }
                }
            }
            if (metricsRead) {
                item(key = "momentum") {
                    RevealOnLoad(delayMillis = 110) {
                        MomentumCard(
                            values = uiState.scoreMomentum,
                            weeklyChange = uiState.scoreWeeklyChange,
                        )
                    }
                }
            }
            item(key = "insights") {
                if (uiState.keyInsights.isNotEmpty()) {
                    RevealOnLoad(delayMillis = 130) {
                        // The moodboard's Key Insights, fed by the real
                        // on-device engine — one insight a day, honestly
                        // sourced, never a fabricated percentage.
                        Column(verticalArrangement = Arrangement.spacedBy(NexusSpacing.sm)) {
                            Text(
                                text = stringResource(R.string.growth_key_insights),
                                style = NexusTypography.DataLabel,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            )
                            for (insight in uiState.keyInsights) {
                                IntelligenceInsight(
                                    headline = insight.title,
                                    evidence = insight.body,
                                    strength = insight.priority.name.uppercase(Locale.ROOT),
                                    source = stringResource(R.string.growth_insight_source),
                                )
                            }
                        }
                    }
                }
            }
            item(key = "radar") {
                when {
                    radarAxes.isEmpty() -> SkillRadarEmpty(onMarkSkills = onSkills)
                    radarAxes.size < 3 -> SkillLevelBreakdown(axes = radarAxes, onMarkSkills = onSkills)
                    else -> SkillRadarChart(radarAxes)
                }
            }
            item(key = "constellation") {
                if (radarAxes.isNotEmpty()) {
                    SkillMapCard(axes = radarAxes)
                }
            }
            if (metricsRead) {
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
            }
            item(key = "goals") {
                if (goalsFault != null) {
                    EditorialErrorState(
                        headline = stringResource(R.string.growth_goals_unavailable),
                        reason = stringResource(R.string.growth_goals_error_reason),
                        actionLabel = stringResource(R.string.growth_try_again),
                        onAction = viewModel::refresh,
                        secondaryHint = goalsFault.takeIf { it.isNotBlank() },
                    )
                } else {
                    GoalsPreview(goals = goals, onGoals = onGoals)
                }
            }
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
            // The same control the header in NexusTopBar uses. A raw IconButton
            // pinned to 32dp is the smallest target in the app, and one-handed
            // reach is the whole point of a back key.
            NexusIconButton(
                icon = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.cd_back),
                onClick = onBack,
                tint = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(end = 4.dp),
            )
        }
        Text(
            text = stringResource(R.string.growth_growth),
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground,
        )
    }
}

/**
 * The score plate. Every figure on it is checkable arithmetic: the headline is the sum of
 * the lines beneath it, and each line states the points earned against the points that
 * input is worth. A bare percentage would ask the reader to trust a number the app cannot
 * show its working for.
 */
@Composable
private fun ScoreLedger(
    score: Int,
    weeklyChange: Int?,
    percentile: Int?,
    readings: List<ProgressionReading>,
) {
    InstrumentPlate(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(NexusSpacing.lg),
        ) {
            CareerProgressionReveal(
                score = score,
                maxScore = Constants.NEXUS_SCORE_MAX,
                readings = readings,
                weeklyChange = weeklyChange,
                percentile = percentile,
            )
            if (score == 0 && readings.isNotEmpty()) {
                Spacer(Modifier.height(NexusSpacing.lg))
                Text(
                    text = stringResource(R.string.growth_start_earning_score),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

/** Display copy for one score input, keyed by the stable identifier the calculator emits. */
@Composable
private fun componentLabel(key: String): String = when (key) {
    "skill" -> stringResource(R.string.growth_component_skill)
    "engagement" -> stringResource(R.string.growth_component_engagement)
    "consistency" -> stringResource(R.string.growth_component_consistency)
    "dsa" -> stringResource(R.string.growth_component_dsa)
    "achievements" -> stringResource(R.string.growth_component_achievements)
    "goals" -> stringResource(R.string.growth_component_goals)
    else -> key
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
 * The career-momentum instrument: the Sagitta score's real daily history from
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
        // Below two readings the chart can only draw its baseline, so a
        // 120dp canvas reads as a large blank card. Collapse to a 56dp strip
        // (MomentumChart honours the caller's height as the max) and let the
        // Voice caption carry the rest. Growth is the only momentum surface
        // that renders the chart while empty — EditorialMetric/Profile guard
        // on size >= 2 already.
        MomentumChart(
            values = values,
            modifier = if (values.size < 2) {
                Modifier.fillMaxWidth().height(56.dp)
            } else {
                Modifier.fillMaxWidth()
            },
        )
        if (values.isEmpty()) {
            Spacer(Modifier.height(10.dp))
            // The instrument is empty because there is nothing to measure yet, not
            // because it failed to load; saying so is the difference between a
            // waiting panel and a broken one.
            Text(
                text = stringResource(R.string.growth_momentum_empty),
                style = NexusTypography.Voice,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        } else {
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

