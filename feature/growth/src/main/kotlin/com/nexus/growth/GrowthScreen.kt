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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nexus.common.Constants
import com.nexus.designsystem.component.InstrumentPlate
import com.nexus.designsystem.component.MomentumChart
import com.nexus.designsystem.component.ConstellationNode
import com.nexus.designsystem.component.NexusCard
import com.nexus.designsystem.component.SkillConstellation
import com.nexus.designsystem.component.SignalField
import com.nexus.designsystem.component.editorial.CareerProgressionReveal
import com.nexus.designsystem.component.editorial.ProgressionReading
import com.nexus.designsystem.effect.RevealOnLoad
import com.nexus.designsystem.theme.NexusColors
import com.nexus.designsystem.theme.NexusSpacing
import java.util.Locale

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
                    ScoreLedger(
                        score = uiState.nexusScore,
                        weeklyChange = weeklyChange,
                        percentile = percentile,
                        readings = readings,
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

