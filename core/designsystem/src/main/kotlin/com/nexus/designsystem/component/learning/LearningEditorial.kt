package com.nexus.designsystem.component.learning

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.nexus.designsystem.component.innerLight
import com.nexus.designsystem.component.pressScale
import com.nexus.designsystem.theme.NexusShapes
import com.nexus.designsystem.theme.NexusSpacing
import com.nexus.designsystem.theme.NexusTypography
import com.nexus.designsystem.theme.nexus
import com.nexus.domain.model.Goal
import com.nexus.domain.model.PortfolioProject
import com.nexus.domain.model.Skill
import com.nexus.domain.model.StudySession
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

/**
 * The learning layer.
 *
 * Proficiency here is always drawn as discrete cells, never as a filled
 * percentage: a self-rated skill on a 0..3 scale does not have a decimal in it,
 * and rendering one would upgrade a student's guess into a measurement.
 */

/** The four named rungs of the 0..3 proficiency scale the domain actually stores. */
private val proficiencyRungs = listOf("NOT RATED", "EXPOSED", "PRACTICED", "APPLIED")

/**
 * A skill and where the user honestly rates themselves on it.
 *
 * Four cells, one per rung — the graphic is the same scale the model stores, so
 * the bar cannot disagree with the label beside it.
 */
@Composable
fun TopicMasteryCard(
    skill: Skill,
    modifier: Modifier = Modifier,
    evidence: String? = null,
    onClick: (() -> Unit)? = null,
) {
    val ext = MaterialTheme.nexus
    val haptic = LocalHapticFeedback.current
    val shape = NexusShapes.md
    val rated = skill.proficiency in 1..3

    Column(
        modifier = modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.pressScale() else Modifier)
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .border(NexusSpacing.hairline, ext.hairline, shape)
            .innerLight(shape)
            .then(
                if (onClick != null) {
                    Modifier.clickable(role = Role.Button) {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onClick()
                    }
                } else {
                    Modifier
                },
            )
            .padding(NexusSpacing.md),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(NexusSpacing.sm),
            verticalAlignment = Alignment.Bottom,
        ) {
            Text(
                text = skill.name,
                style = NexusTypography.TitleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = if (rated) proficiencyRungs[skill.proficiency] else "NOT RATED",
                style = NexusTypography.DataLabel,
                color = if (rated) ext.signal else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(Modifier.height(NexusSpacing.sm))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(5.dp),
            horizontalArrangement = Arrangement.spacedBy(NexusSpacing.xxxs),
        ) {
            // Three fixed cells, one per rated rung: the scale is ordinal, so
            // a continuous fill would imply precision the data does not have.
            for (rung in 1..3) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .background(
                            if (rated && skill.proficiency >= rung) {
                                ext.signal
                            } else {
                                ext.hairline
                            },
                            NexusShapes.tag,
                        ),
                )
            }
            Box(
                modifier = Modifier
                    .weight(0.4f)
                    .fillMaxHeight(),
            )
        }
        Spacer(Modifier.height(NexusSpacing.sm))
        Text(
            text = evidence ?: skill.category.name.replace('_', ' ').lowercase()
                .replaceFirstChar { it.titlecase(Locale.ROOT) },
            style = NexusTypography.BodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

/** One stop on a skill journey. */
data class JourneyStage(
    val title: String,
    val window: String = "",
    val itemsDone: Int = 0,
    val itemsTotal: Int = 0,
    val isCurrent: Boolean = false,
)

/**
 * A roadmap as a vertical track with the current stage named.
 *
 * Counts are shown as `done of total` rather than a percentage for the same
 * reason as skill overlap: they are checkable against the list below them.
 */
@Composable
fun SkillJourneyTimeline(
    stages: List<JourneyStage>,
    modifier: Modifier = Modifier,
    onSelect: ((Int) -> Unit)? = null,
) {
    val ext = MaterialTheme.nexus
    val haptic = LocalHapticFeedback.current
    if (stages.isEmpty()) return

    Column(modifier = modifier.fillMaxWidth()) {
        stages.forEachIndexed { index, stage ->
            val isLast = index == stages.lastIndex
            Row(modifier = Modifier.fillMaxWidth()) {
                // The spine is one rule with a node per stage; a completed node
                // is filled, the current node is a ring, a future one a dot.
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(11.dp)
                            .clip(NexusShapes.tag)
                            .background(
                                when {
                                    stage.isCurrent -> ext.signal
                                    stage.itemsTotal > 0 && stage.itemsDone >= stage.itemsTotal ->
                                        ext.structure
                                    else -> Color.Transparent
                                },
                            )
                            .then(
                                if (stage.isCurrent ||
                                    (stage.itemsTotal > 0 && stage.itemsDone >= stage.itemsTotal)
                                ) {
                                    Modifier
                                } else {
                                    Modifier.border(
                                        NexusSpacing.hairline,
                                        ext.hairlineStrong,
                                        NexusShapes.tag,
                                    )
                                },
                            ),
                    )
                    if (!isLast) {
                        Box(
                            modifier = Modifier
                                .width(NexusSpacing.hairline)
                                .height(34.dp)
                                .background(ext.hairline),
                        )
                    }
                }
                Spacer(Modifier.width(NexusSpacing.md))
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(bottom = if (isLast) 0.dp else NexusSpacing.md)
                        .then(
                            if (onSelect != null) {
                                Modifier
                                    .clip(NexusShapes.xs)
                                    .clickable(role = Role.Button) {
                                        haptic.performHapticFeedback(
                                            HapticFeedbackType.TextHandleMove,
                                        )
                                        onSelect(index)
                                    }
                            } else {
                                Modifier
                            },
                        ),
                ) {
                    Text(
                        text = stage.title,
                        style = NexusTypography.TitleMedium,
                        color = if (stage.isCurrent) {
                            MaterialTheme.colorScheme.onSurface
                        } else {
                            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
                        },
                    )
                    val meta = listOf(
                        stage.window.takeIf { it.isNotBlank() },
                        if (stage.itemsTotal > 0) {
                            "${stage.itemsDone} of ${stage.itemsTotal} done"
                        } else {
                            null
                        },
                    ).filterNotNull()
                    if (meta.isNotEmpty()) {
                        Spacer(Modifier.height(NexusSpacing.xxxs))
                        Text(
                            text = meta.joinToString("  ·  "),
                            style = NexusTypography.TelemetryMicro,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        )
                    }
                    if (stage.isCurrent) {
                        Spacer(Modifier.height(NexusSpacing.xxs))
                        Text(
                            text = "YOU ARE HERE".uppercase(Locale.ROOT),
                            style = NexusTypography.DataLabel,
                            color = ext.signal,
                        )
                    }
                }
            }
        }
    }
}

/**
 * One recorded study session.
 *
 * Duration is the fact; [StudySession.focusScore] is self-reported, so it is
 * drawn as a rung label rather than a decimal.
 */
@Composable
fun StudySessionCard(
    session: StudySession,
    modifier: Modifier = Modifier,
) {
    val ext = MaterialTheme.nexus
    val hours = session.durationMinutes / 60
    val minutes = session.durationMinutes % 60
    val duration = when {
        hours > 0 -> "${hours}h ${minutes}m"
        else -> "${minutes}m"
    }
    val focusLabel = when {
        session.focusScore >= 0.8f -> "DEEP"
        session.focusScore >= 0.5f -> "MIXED"
        else -> "SCATTERED"
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(NexusShapes.sm)
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .border(NexusSpacing.hairline, ext.hairline, NexusShapes.sm)
            .padding(horizontal = NexusSpacing.md, vertical = NexusSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = duration,
            style = NexusTypography.Telemetry,
            color = ext.signal,
            modifier = Modifier.width(56.dp),
        )
        Spacer(Modifier.width(NexusSpacing.md))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = session.topic.ifBlank { "Untitled session" },
                style = NexusTypography.BodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = session.startTime.atZone(java.time.ZoneId.systemDefault())
                    .format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)),
                style = NexusTypography.TelemetryMicro,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
            )
        }
        Text(
            text = focusLabel,
            style = NexusTypography.DataLabel,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

/**
 * A project as the portfolio entry it is.
 *
 * Links render only when the URL exists, and stars are counted rather than
 * formatted into a rank — a project with two stars is two stars.
 */
@Composable
fun ProjectPortfolioCard(
    project: PortfolioProject,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    val ext = MaterialTheme.nexus
    val haptic = LocalHapticFeedback.current
    val shape = NexusShapes.md

    Column(
        modifier = modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.pressScale() else Modifier)
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .border(
                NexusSpacing.hairline,
                if (project.isFeatured) ext.signal.copy(alpha = 0.4f) else ext.hairline,
                shape,
            )
            .innerLight(shape)
            .then(
                if (onClick != null) {
                    Modifier.clickable(role = Role.Button) {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onClick()
                    }
                } else {
                    Modifier
                },
            )
            .padding(NexusSpacing.md),
    ) {
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = project.title,
                style = NexusTypography.EditorialTitle,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            if (project.isFeatured) {
                Text(
                    text = "FEATURED".uppercase(Locale.ROOT),
                    style = NexusTypography.DataLabel,
                    color = ext.signal,
                    modifier = Modifier.padding(start = NexusSpacing.sm, bottom = 4.dp),
                )
            }
        }
        if (project.description.isNotBlank()) {
            Spacer(Modifier.height(NexusSpacing.xs))
            Text(
                text = project.description,
                style = NexusTypography.BodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )
        }
        if (project.techStack.isNotEmpty()) {
            Spacer(Modifier.height(NexusSpacing.md))
            HorizontalDivider(thickness = NexusSpacing.hairline, color = ext.hairline)
            Spacer(Modifier.height(NexusSpacing.sm))
            Text(
                text = project.techStack.joinToString("  ·  "),
                style = NexusTypography.TelemetryMicro,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Spacer(Modifier.height(NexusSpacing.sm))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(NexusSpacing.lg),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (project.starCount > 0) {
                Text(
                    text = "${project.starCount} STARS",
                    style = NexusTypography.DataLabel,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            // Absent links are absent; a greyed-out "no demo" is noise.
            if (project.githubUrl != null) {
                Text(
                    text = "CODE",
                    style = NexusTypography.DataLabel,
                    color = ext.structure,
                )
            }
            if (project.liveUrl != null) {
                Text(
                    text = "LIVE",
                    style = NexusTypography.DataLabel,
                    color = ext.structure,
                )
            }
        }
    }
}

/**
 * A goal with its milestones beside the progress, not under it.
 *
 * [Goal.progress] is a float the caller maintains, so it is drawn as a rule and
 * restated as a milestone count — if the two disagree, the count is the one a
 * user can verify by tapping.
 */
@Composable
fun LearningGoalCard(
    goal: Goal,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    val ext = MaterialTheme.nexus
    val haptic = LocalHapticFeedback.current
    val shape = NexusShapes.md
    val done = goal.milestones.count { it.isCompleted }
    val total = goal.milestones.size

    Column(
        modifier = modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.pressScale() else Modifier)
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .border(NexusSpacing.hairline, ext.hairline, shape)
            .innerLight(shape)
            .then(
                if (onClick != null) {
                    Modifier.clickable(role = Role.Button) {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onClick()
                    }
                } else {
                    Modifier
                },
            )
            .padding(NexusSpacing.md)
            .semantics(mergeDescendants = true) {},
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (goal.isCompleted) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = null,
                    tint = ext.positive,
                    modifier = Modifier.size(15.dp),
                )
                Spacer(Modifier.width(NexusSpacing.sm))
            }
            Text(
                text = goal.title,
                style = NexusTypography.TitleMedium,
                color = if (goal.isCompleted) {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
                modifier = Modifier.weight(1f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(Modifier.width(NexusSpacing.sm))
            Text(
                text = goal.category.name.replace('_', ' ').uppercase(Locale.ROOT),
                style = NexusTypography.DataLabel,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            )
        }
        Spacer(Modifier.height(NexusSpacing.md))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .background(ext.hairline, NexusShapes.tag),
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth(goal.progress.coerceIn(0f, 1f))
                    .fillMaxHeight()
                    .background(
                        if (goal.isCompleted) ext.positive else ext.signal,
                        NexusShapes.tag,
                    ),
            )
        }
        Spacer(Modifier.height(NexusSpacing.sm))
        val meta = buildList {
            if (total > 0) add("$done of $total milestones")
            goal.targetDate?.let { add("BY $it") }
        }
        if (meta.isNotEmpty()) {
            Text(
                text = meta.joinToString("  ·  ").uppercase(Locale.ROOT),
                style = NexusTypography.TelemetryMicro,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            )
        }
    }
}
