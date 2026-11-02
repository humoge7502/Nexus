package com.nexus.designsystem.component.metric

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.nexus.designsystem.component.MomentumChart
import com.nexus.designsystem.component.Readout
import com.nexus.designsystem.theme.NexusShapes
import com.nexus.designsystem.theme.NexusSpacing
import com.nexus.designsystem.theme.NexusTypography
import com.nexus.designsystem.theme.nexus
import java.util.Locale

/**
 * The metric family: a number, its direction, and the context that makes the
 * direction mean something.
 *
 * Three shapes cover every figure the product reports — a value over time, a
 * value against a comparison the data actually carries, and a value split into
 * parts. A benchmark that no dataset supplies renders as absent rather than
 * guessed, because an invented cohort average is the fastest way to teach a
 * student to distrust every number on this screen.
 */

/** Direction of travel, carried as text as well as sign so it survives TalkBack. */
enum class MetricDirection { Up, Down, Flat }

private fun MetricDirection.glyph(): String = when (this) {
    MetricDirection.Up -> "▲"
    MetricDirection.Down -> "▼"
    MetricDirection.Flat -> "—"
}

/**
 * A measured value with its recent history attached.
 *
 * [series] is optional: with fewer than two points there is no trend to draw,
 * and a sparkline of one dot is a lie about a shape.
 */
@Composable
fun MetricTrend(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    unit: String? = null,
    series: List<Float> = emptyList(),
    delta: String? = null,
    direction: MetricDirection = MetricDirection.Flat,
    isDeltaFavourable: Boolean = true,
    caption: String? = null,
) {
    val ext = MaterialTheme.nexus
    val deltaColor = when {
        delta == null -> MaterialTheme.colorScheme.onSurfaceVariant
        direction == MetricDirection.Flat -> MaterialTheme.colorScheme.onSurfaceVariant
        isDeltaFavourable -> ext.positive
        else -> ext.attention
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) {},
    ) {
        Readout(value = value, label = label, unit = unit)
        if (delta != null) {
            Spacer(Modifier.height(NexusSpacing.xxs))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = direction.glyph(),
                    style = NexusTypography.TelemetryMicro,
                    color = deltaColor,
                )
                Spacer(Modifier.width(NexusSpacing.xxs))
                Text(
                    text = delta,
                    style = NexusTypography.Telemetry,
                    color = deltaColor,
                )
            }
        }
        // Two points define a line; one defines nothing, so the plate is skipped.
        if (series.size >= 2) {
            Spacer(Modifier.height(NexusSpacing.sm))
            MomentumChart(
                values = series,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(34.dp),
            )
        }
        if (!caption.isNullOrBlank()) {
            Spacer(Modifier.height(NexusSpacing.xs))
            Text(
                text = caption,
                style = NexusTypography.BodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
            )
        }
    }
}

/**
 * The user's figure set against one the data actually reports.
 *
 * The reference tick is drawn only when [benchmark] exists. The bar is a
 * position, not a score — it never implies the maximum is a target.
 */
@Composable
fun MetricComparison(
    label: String,
    value: String,
    valueFraction: Float?,
    modifier: Modifier = Modifier,
    benchmark: Float? = null,
    benchmarkLabel: String? = null,
    benchmarkValue: String? = null,
    domain: ClosedFloatingPointRange<Float> = 0f..1f,
) {
    val ext = MaterialTheme.nexus
    val span = (domain.endInclusive - domain.start).coerceAtLeast(0.0001f)
    val filled = valueFraction?.let { ((it - domain.start) / span).coerceIn(0f, 1f) }
    val marker = benchmark?.let { ((it - domain.start) / span).coerceIn(0f, 1f) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) {},
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(
                text = label.uppercase(Locale.ROOT),
                style = NexusTypography.DataLabel,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = value,
                style = NexusTypography.TelemetryLarge,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }
        Spacer(Modifier.height(NexusSpacing.sm))
        if (filled != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .fillMaxHeight()
                        .background(ext.hairline, NexusShapes.tag),
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth(filled)
                        .fillMaxHeight()
                        .background(ext.signal, NexusShapes.tag),
                )
                if (marker != null) {
                    // A rule crossing the bar, not a second fill: the comparison
                    // is a position, so the two never read as competing scores.
                    // fillMaxWidth puts the marker on the box's right edge.
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .fillMaxWidth(marker)
                            .fillMaxHeight()
                            .width(2.dp)
                            .background(ext.structure),
                    )
                }
            }
        }
        if (marker != null && benchmarkValue != null) {
            Spacer(Modifier.height(NexusSpacing.xs))
            Text(
                text = "${benchmarkLabel ?: "REFERENCE"} · $benchmarkValue",
                style = NexusTypography.TelemetryMicro,
                color = ext.structure,
            )
        }
    }
}

/** One slice of a divided total. */
data class MetricPart(
    val label: String,
    val value: Int,
    val color: Color?,
)

/**
 * A total split into named counts.
 *
 * The bar is drawn from the parts, so it cannot disagree with the legend beside
 * it — the usual failure of a chart wired to a separately-handled list.
 */
@Composable
fun MetricBreakdown(
    label: String,
    parts: List<MetricPart>,
    modifier: Modifier = Modifier,
    barHeight: Dp = 8.dp,
) {
    val ext = MaterialTheme.nexus
    val total = parts.sumOf { it.value }
    if (parts.isEmpty() || total <= 0) return

    Column(
        modifier = modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = true) {},
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom,
        ) {
            Text(
                text = label.uppercase(Locale.ROOT),
                style = NexusTypography.DataLabel,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = total.toString(),
                style = NexusTypography.Telemetry,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(Modifier.height(NexusSpacing.sm))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(barHeight),
            horizontalArrangement = Arrangement.spacedBy(NexusSpacing.xxxs),
        ) {
            parts.filter { it.value > 0 }.forEach { part ->
                Box(
                    modifier = Modifier
                        .weight(part.value.toFloat())
                        .fillMaxHeight()
                        .background(
                            part.color ?: ext.hairlineStrong,
                            NexusShapes.tag,
                        ),
                )
            }
        }
        Spacer(Modifier.height(NexusSpacing.sm))
        HorizontalDivider(thickness = NexusSpacing.hairline, color = ext.hairline)
        Spacer(Modifier.height(NexusSpacing.sm))
        parts.forEach { part ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = NexusSpacing.xxxs),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .width(8.dp)
                        .height(2.dp)
                        .background(part.color ?: ext.hairlineStrong),
                )
                Spacer(Modifier.width(NexusSpacing.sm))
                Text(
                    text = part.label,
                    style = NexusTypography.BodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = part.value.toString(),
                    style = NexusTypography.Telemetry,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.End,
                )
            }
        }
    }
}
