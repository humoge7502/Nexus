package com.nexus.designsystem.chart

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.dp
import com.nexus.designsystem.R
import com.nexus.designsystem.theme.NexusSpacing
import com.nexus.designsystem.theme.NexusTypography
import com.nexus.designsystem.theme.nexus

/** One day of recorded activity. [date] is an ISO-8601 `yyyy-MM-dd` string. */
data class ActivityDay(val date: String, val count: Int)

/** A column of consecutive days, Monday first. */
data class ActivityWeek(val days: List<ActivityDay>)

/**
 * A term of activity as a grid, not a number.
 *
 * A streak told as "18 days" is one fact; told as a grid it is eighteen, and the
 * shape of a term — the gap over exam week, the density before a deadline — is
 * what a student can actually act on. Intensity scales against the busiest day
 * in the window rather than a fixed constant, so the same component reads
 * correctly for study minutes, problems solved, or applications sent.
 */
@Composable
fun LearningHeatmap(
    weeks: List<ActivityWeek>,
    modifier: Modifier = Modifier,
    today: String? = null,
    accent: Color = MaterialTheme.nexus.positive,
    emptyColor: Color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f),
    todayRing: Color = MaterialTheme.nexus.signal,
    cellSize: Int = 11,
    cellSpacing: Int = 3,
    label: String? = null,
) {
    if (weeks.isEmpty()) return

    val dayCount = weeks.sumOf { it.days.size }
    val activeDays = weeks.sumOf { week -> week.days.count { it.count > 0 } }
    val total = weeks.sumOf { week -> week.days.sumOf { it.count } }
    val peak = (weeks.maxOfOrNull { week -> week.days.maxOfOrNull { it.count } ?: 0 } ?: 0)
        .coerceAtLeast(1)

    val description = stringResource(R.string.cd_activity_heatmap, weeks.size, activeDays, total)
    val cell = cellSize.dp
    val pitch = cellSize.dp + cellSpacing.dp

    fun intensityAlpha(count: Int): Float = when {
        count <= 0 -> 0f
        peak <= 1 -> 0.85f
        else -> 0.28f + 0.67f * (count.toFloat() / peak)
    }

    Column(modifier = modifier.fillMaxWidth()) {
        if (!label.isNullOrBlank()) {
            Text(
                text = label.uppercase(),
                style = NexusTypography.DataLabel,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = NexusSpacing.sm),
            )
        }

        // The grid scrolls rather than shrinking, because a cell that is 4dp
        // wide stops being a cell and becomes texture.
        Canvas(
            modifier = Modifier
                .horizontalScroll(rememberScrollState())
                .width(pitch * weeks.size - cellSpacing.dp)
                .height(pitch * 7 - cellSpacing.dp)
                .clearAndSetSemantics { contentDescription = description },
        ) {
            val radius = CornerRadius(2.dp.toPx())
            val side = cell.toPx()
            val step = pitch.toPx()
            val hairline = with(density) { 1.dp.toPx() }

            weeks.forEachIndexed { weekIndex, week ->
                week.days.forEachIndexed { dayIndex, day ->
                    val left = weekIndex * step
                    val top = dayIndex * step
                    val alpha = intensityAlpha(day.count)
                    drawRoundRect(
                        color = if (alpha > 0f) accent.copy(alpha = alpha) else emptyColor,
                        topLeft = Offset(left, top),
                        size = Size(side, side),
                        cornerRadius = radius,
                    )
                    // Today is ringed, not filled brighter: the ring is a shape,
                    // so it survives greyscale and never fakes activity.
                    if (today != null && day.date == today) {
                        drawRoundRect(
                            color = todayRing,
                            topLeft = Offset(left + hairline / 2f, top + hairline / 2f),
                            size = Size(side - hairline, side - hairline),
                            cornerRadius = radius,
                            style = Stroke(width = hairline),
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(NexusSpacing.md))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = "$activeDays of $dayCount days active",
                style = NexusTypography.TelemetryMicro,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                // A five-step key with its ceiling named, so the ramp is a claim
                // about intensity rather than a decorative gradient.
                listOf(0f, 0.25f, 0.5f, 0.75f, 1f).forEach { step ->
                    Swatch(
                        color = if (step == 0f) {
                            emptyColor
                        } else {
                            accent.copy(alpha = 0.28f + 0.67f * step)
                        },
                    )
                    Spacer(Modifier.width(2.dp))
                }
                Text(
                    text = "PEAK $peak",
                    style = NexusTypography.TelemetryMicro,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                )
            }
        }
    }
}

@Composable
private fun Swatch(color: Color) {
    Canvas(modifier = Modifier.size(8.dp)) {
        drawRoundRect(
            color = color,
            size = Size(size.width, size.height),
            cornerRadius = CornerRadius(2.dp.toPx()),
        )
    }
}
