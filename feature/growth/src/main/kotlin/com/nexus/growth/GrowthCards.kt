package com.nexus.growth

import androidx.compose.ui.res.stringResource
import com.nexus.growth.R

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.nexus.designsystem.component.AnimatedStatCard
import com.nexus.designsystem.component.CalibratedRule
import com.nexus.designsystem.component.LeaderRule
import com.nexus.designsystem.component.NexusButton
import com.nexus.designsystem.component.NexusCard
import com.nexus.designsystem.component.SignalTag
import com.nexus.designsystem.component.SignalTone
import com.nexus.designsystem.theme.NexusColors
import com.nexus.designsystem.theme.NexusGradients
import com.nexus.designsystem.theme.NexusShapes
import com.nexus.designsystem.theme.NexusSpacing
import com.nexus.designsystem.theme.NexusTypography
import com.nexus.designsystem.theme.nexus
import java.time.LocalDate
import java.util.Locale
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

@Composable
internal fun MetricBar(metric: Metric) {
    Row(
        modifier = Modifier.fillMaxWidth().height(24.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            metric.label,
            style = MaterialTheme.typography.bodySmall,
            color = NexusColors.Bone.copy(alpha = 0.80f),
        )
        LeaderRule(
            modifier = Modifier.weight(1f),
            color = NexusColors.Bone.copy(alpha = 0.28f),
        )
        Text(
            stringResource(R.string.growth_progress_percent, (metric.progress * 100).toInt()),
            style = NexusTypography.TelemetryMicro,
            color = NexusColors.Bone,
        )
    }
}

@Composable
internal fun SkillRadarChart(axes: List<RadarAxis>) {
    val gridColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
    val accent = MaterialTheme.colorScheme.primary
    val dotCore = MaterialTheme.colorScheme.surfaceContainerLow

    NexusCard(shape = NexusShapes.lg) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.growth_skill_radar),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )

            Spacer(Modifier.height(NexusSpacing.md))

            val animatedValues = remember { axes.map { mutableFloatStateOf(0f) } }
            LaunchedEffect(Unit) {
                animatedValues.forEachIndexed { index, state ->
                    kotlinx.coroutines.delay(index * 100L)
                    state.floatValue = axes[index].value
                }
            }

            val radarDescription = stringResource(
                R.string.growth_skill_radar_description,
                axes.joinToString { "${it.label} ${(it.value * 100).roundToInt()}%" },
            )
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .padding(NexusSpacing.sm)
                    .clearAndSetSemantics { contentDescription = radarDescription },
            ) {
                val cx = size.width / 2f
                val cy = size.height / 2f
                val radius = minOf(cx, cy) * 0.85f
                val angleStep = 2f * PI.toFloat() / axes.size
                val startAngle = -PI.toFloat() / 2f

                for (level in 1..4) {
                    val r = radius * level / 4f
                    val path = Path()
                    axes.indices.forEach { i ->
                        val angle = startAngle + i * angleStep
                        val x = cx + r * cos(angle)
                        val y = cy + r * sin(angle)
                        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                    }
                    path.close()
                    drawPath(path, gridColor, style = Stroke(1.dp.toPx()))
                }

                val dataPath = Path()
                axes.indices.forEach { i ->
                    val angle = startAngle + i * angleStep
                    val r = radius * animatedValues[i].floatValue
                    val x = cx + r * cos(angle)
                    val y = cy + r * sin(angle)
                    if (i == 0) dataPath.moveTo(x, y) else dataPath.lineTo(x, y)
                }
                dataPath.close()

                drawPath(
                    dataPath,
                    brush = Brush.linearGradient(
                        listOf(accent.copy(alpha = 0.32f), accent.copy(alpha = 0.04f)),
                    ),
                )
                drawPath(dataPath, accent, style = Stroke(2.dp.toPx(), cap = StrokeCap.Round))

                axes.indices.forEach { i ->
                    val angle = startAngle + i * angleStep
                    val r = radius * animatedValues[i].floatValue
                    val x = cx + r * cos(angle)
                    val y = cy + r * sin(angle)

                    drawCircle(accent, 4.dp.toPx(), Offset(x, y))
                    drawCircle(dotCore, 2.dp.toPx(), Offset(x, y))
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                axes.forEach { axis ->
                    Text(
                        axis.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.width(40.dp),
                        maxLines = 1,
                    )
                }
            }
        }
    }
}

@Composable
internal fun SkillRadarEmpty() {
    NexusCard(shape = NexusShapes.lg) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.growth_skill_radar),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.height(NexusSpacing.sm))
            Text(stringResource(R.string.growth_mark_the_skills_you_already_know),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
internal fun LearningStreak(
    currentStreak: Int,
    longestStreak: Int,
    thisMonthActive: Int,
    weekActivity: List<Boolean>,
) {
    val ext = MaterialTheme.nexus
    val muted = MaterialTheme.colorScheme.onSurfaceVariant

    NexusCard(shape = NexusShapes.lg) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.LocalFireDepartment,
                    contentDescription = null,
                    tint = ext.attention,
                    modifier = Modifier.size(16.dp),
                )
                Spacer(Modifier.width(NexusSpacing.xs))
                Text(
                    stringResource(R.string.arena_streak).uppercase(Locale.ROOT),
                    style = NexusTypography.DataLabel,
                    color = muted,
                    modifier = Modifier.weight(1f),
                )
                if (currentStreak > 0) {
                    Text(
                        stringResource(R.string.growth_this_month, thisMonthActive),
                        style = NexusTypography.TelemetryMicro,
                        color = muted,
                    )
                }
            }

            Spacer(Modifier.height(NexusSpacing.sm))

            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    if (currentStreak > 0) currentStreak.toString() else "0",
                    style = NexusTypography.TelemetryDisplay,
                    color = if (currentStreak > 0) ext.attention else muted,
                )
                Spacer(Modifier.width(NexusSpacing.xs))
                Text(
                    if (currentStreak > 0) {
                        stringResource(R.string.growth_days)
                    } else {
                        stringResource(R.string.growth_start_today)
                    },
                    style = NexusTypography.DataLabel,
                    color = muted,
                    modifier = Modifier
                        .weight(1f)
                        .padding(bottom = NexusSpacing.xs),
                )
                if (longestStreak > 0) {
                    Text(
                        stringResource(R.string.growth_best_streak, longestStreak)
                            .uppercase(Locale.ROOT),
                        style = NexusTypography.DataLabel,
                        color = muted,
                        modifier = Modifier.padding(bottom = NexusSpacing.xs),
                    )
                }
            }

            Spacer(Modifier.height(NexusSpacing.md))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(NexusSpacing.xs),
            ) {
                for (index in 0 until 7) {
                    val date = LocalDate.now().minusDays((6 - index).toLong())
                    val isToday = index == 6
                    val isActive = weekActivity.getOrElse(index) { false }
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(26.dp)
                                .clip(NexusShapes.tag)
                                .background(
                                    if (isActive) ext.signal
                                    else MaterialTheme.colorScheme.surfaceContainerHigh,
                                ),
                        )
                        Spacer(Modifier.height(NexusSpacing.xs))
                        Text(
                            date.dayOfWeek.name.take(1),
                            style = NexusTypography.DataLabel,
                            color = if (isActive) {
                                MaterialTheme.colorScheme.onSurface
                            } else {
                                muted
                            },
                        )
                        Spacer(Modifier.height(NexusSpacing.xxs))
                        if (isToday) {
                            Box(
                                modifier = Modifier
                                    .width(2.dp)
                                    .height(11.dp)
                                    .background(
                                        color = if (isActive) ext.signal else ext.hairlineStrong,
                                        shape = NexusShapes.tag,
                                    ),
                            )
                        } else {
                            Spacer(Modifier.height(11.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun QuickStatsRow(
    problemsSolved: Int,
    goalsCompleted: Int,
    projects: Int,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(NexusSpacing.sm),
    ) {
        AnimatedStatCard(
            value = problemsSolved,
            label = stringResource(R.string.growth_problems_solved),
            modifier = Modifier.weight(1f),
        )
        AnimatedStatCard(
            value = goalsCompleted,
            label = stringResource(R.string.growth_goals_done),
            modifier = Modifier.weight(1f),
        )
        AnimatedStatCard(
            value = projects,
            label = stringResource(R.string.growth_projects),
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
internal fun GoalsPreview(
    goals: List<com.nexus.domain.model.Goal>,
    onGoals: () -> Unit,
) {
    val ext = MaterialTheme.nexus

    NexusCard(onClick = onGoals, shape = NexusShapes.lg) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(stringResource(R.string.growth_your_goals),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(stringResource(R.string.growth_view_all),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            Spacer(Modifier.height(NexusSpacing.md))

            if (goals.isEmpty()) {
                Text(stringResource(R.string.growth_no_goals_yet_set_one_and),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            } else {
                goals.take(3).forEach { goal ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = NexusSpacing.xxs),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = if (goal.isCompleted) Icons.Default.CheckCircle
                            else Icons.Default.RadioButtonUnchecked,
                            contentDescription = null,
                            tint = if (goal.isCompleted) ext.positive
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(Modifier.width(NexusSpacing.sm))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                goal.title,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium,
                                color = if (goal.isCompleted) ext.positive
                                else MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                            )
                            Spacer(Modifier.height(NexusSpacing.xs))
                            val progress by animateFloatAsState(
                                targetValue = if (goal.isCompleted) 1f else goal.progress,
                                animationSpec = spring(dampingRatio = Spring.DampingRatioNoBouncy),
                                label = "goal_${goal.id}",
                            )
                            CalibratedRule(
                                progress = progress,
                                modifier = Modifier.fillMaxWidth(),
                                height = 16.dp,
                                accent = if (goal.isCompleted) ext.positive
                                else MaterialTheme.colorScheme.primary,
                            )
                            Spacer(Modifier.height(NexusSpacing.xxs))
                            Text(
                                if (goal.isCompleted) {
                                    stringResource(R.string.growth_completed)
                                } else {
                                    stringResource(
                                        R.string.growth_percent_complete,
                                        (goal.progress * 100).toInt(),
                                    )
                                },
                                style = NexusTypography.TelemetryMicro,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
internal fun NexusActionsRow(onOracle: () -> Unit, onArena: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(NexusSpacing.sm),
    ) {
        ActionCard(
            icon = Icons.Default.AutoAwesome,
            title = stringResource(R.string.growth_oracle),
            subtitle = stringResource(R.string.growth_career_forecast),
            onClick = onOracle,
            modifier = Modifier.weight(1f),
        )
        ActionCard(
            icon = Icons.Default.EmojiEvents,
            title = stringResource(R.string.growth_arena),
            subtitle = stringResource(R.string.growth_compete_grow),
            onClick = onArena,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun ActionCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    NexusCard(
        onClick = onClick,
        modifier = modifier,
        shape = NexusShapes.md,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(22.dp),
        )
        Spacer(Modifier.height(NexusSpacing.sm))
        Text(
            title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            subtitle,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
internal fun TodaysChallenge(
    challenge: com.nexus.domain.model.DSAProblem?,
    onStart: () -> Unit,
) {
    if (challenge == null) return

    val difficultyLabel = when (challenge.difficulty) {
        com.nexus.domain.model.DsaDifficulty.EASY -> "Easy"
        com.nexus.domain.model.DsaDifficulty.MEDIUM -> "Medium"
        com.nexus.domain.model.DsaDifficulty.HARD -> "Hard"
    }
    val difficultyTone = when (challenge.difficulty) {
        com.nexus.domain.model.DsaDifficulty.EASY -> SignalTone.Signal
        com.nexus.domain.model.DsaDifficulty.MEDIUM -> SignalTone.Attention
        com.nexus.domain.model.DsaDifficulty.HARD -> SignalTone.Critical
    }

    NexusCard(shape = NexusShapes.lg) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.growth_today_s_challenge),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )

            Spacer(Modifier.height(NexusSpacing.sm))

            Row(verticalAlignment = Alignment.CenterVertically) {
                SignalTag(text = difficultyLabel, tone = difficultyTone)
                Spacer(Modifier.width(NexusSpacing.sm))
                Text(
                    challenge.title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    challenge.platform,
                    style = NexusTypography.DataLabel,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(Modifier.height(NexusSpacing.md))

            NexusButton(
                text = stringResource(R.string.growth_start_challenge),
                onClick = onStart,
                modifier = Modifier.fillMaxWidth(),
                gradient = NexusGradients.PrimaryDeep,
            )
        }
    }
}
