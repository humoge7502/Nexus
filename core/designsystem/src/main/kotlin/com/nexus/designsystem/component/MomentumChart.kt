package com.nexus.designsystem.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import com.nexus.designsystem.theme.NexusAnimation
import com.nexus.designsystem.theme.NexusTypography
import com.nexus.designsystem.theme.motionMillis
import com.nexus.designsystem.theme.nexus
import java.util.Locale
import kotlin.math.roundToInt

/**
 * The career-momentum instrument: the user's Sagitta-score history drawn as one
 * progressive line with a soft area under it, in the signal color.
 *
 * Data contract: [values] are chronological score readings (any scale; the
 * chart normalises to its own min/max window). Fewer than two readings render
 * the baseline only — the chart never invents history. The reveal is a single
 * left-to-right wipe driven by [NexusAnimation]; with reduced motion it
 * appears fully drawn immediately.
 *
 * The chart is a readable instrument, not a picture: dragging horizontally
 * scrubs a hairline across the readings with the value in mono — the same
 * reading the plot itself carries, never a derived one. Scrubbing is direct
 * manipulation, so it stays available under reduced motion; the readout
 * simply appears without tweening.
 */
@Composable
fun MomentumChart(
    values: List<Float>,
    modifier: Modifier = Modifier,
    /** TalkBack label. Defaults to a summary computed from the real readings. */
    chartLabel: String? = null,
) {
    val extended = MaterialTheme.nexus
    var revealed by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { revealed = true }
    val reveal by animateFloatAsState(
        targetValue = if (revealed) 1f else 0f,
        animationSpec = tween(
            durationMillis = motionMillis(NexusAnimation.Duration.long),
            easing = NexusAnimation.Easing.emphasizedDecelerate,
        ),
        label = "momentumReveal",
    )
    var scrubX by remember { mutableStateOf<Float?>(null) }
    val textMeasurer = rememberTextMeasurer()
    val scrubInk = MaterialTheme.colorScheme.onSurface

    val cleanValues = remember(values) { values.filter { it.isFinite() } }

    // The plot is a real instrument, so it reads aloud as one: a summary
    // computed from the actual readings, never invented. Fewer than two
    // readings is announced as exactly that.
    val chartSummary = remember(cleanValues, chartLabel) {
        chartLabel ?: if (cleanValues.size < 2) {
            "Momentum chart, ${if (cleanValues.isEmpty()) "no" else "one"} reading yet"
        } else {
            val first = cleanValues.first()
            val last = cleanValues.last()
            val peak = cleanValues.max()
            "Momentum chart: ${cleanValues.size} readings from " +
                String.format(Locale.US, "%.1f", first) + " to " +
                String.format(Locale.US, "%.1f", last) + ", peak " +
                String.format(Locale.US, "%.1f", peak)
        }
    }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(120.dp)
            .semantics { contentDescription = chartSummary }
            .pointerInput(cleanValues) {
                // Horizontal intent only: a vertical drag still scrolls the
                // page; a horizontal one reads the instrument.
                detectHorizontalDragGestures(
                    onDragStart = { offset -> scrubX = offset.x },
                    onHorizontalDrag = { change, _ ->
                        change.consume()
                        scrubX = change.position.x
                    },
                    onDragEnd = { scrubX = null },
                    onDragCancel = { scrubX = null },
                )
            },
    ) {
        val w = size.width
        val h = size.height
        // The head band reserves room for the scrub readout so the label never
        // covers the line it is reading.
        val padTop = h * 0.16f
        val padBottom = h * 0.12f
        val plotH = h - padTop - padBottom

        // Baseline grid: dashed hairlines, per the instrument grid language.
        val gridEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 10f))
        drawLine(
            color = extended.gridLine,
            start = Offset(0f, padTop),
            end = Offset(w, padTop),
            pathEffect = gridEffect,
        )
        drawLine(
            color = extended.gridLine,
            start = Offset(0f, padTop + plotH / 2f),
            end = Offset(w, padTop + plotH / 2f),
            pathEffect = gridEffect,
        )
        drawLine(
            color = extended.hairlineStrong,
            start = Offset(0f, padTop + plotH),
            end = Offset(w, padTop + plotH),
        )

        if (cleanValues.isEmpty()) return@Canvas

        val minV = cleanValues.min()
        val maxV = cleanValues.max()
        val span = (maxV - minV).coerceAtLeast(1f)
        val stepX = if (cleanValues.size == 1) 0f else w / (cleanValues.size - 1)

        fun pointAt(i: Int): Offset {
            val x = if (cleanValues.size == 1) w / 2f else i * stepX
            val y = padTop + plotH - ((cleanValues[i] - minV) / span) * plotH
            return Offset(x, y)
        }

        val line = Path()
        val area = Path()
        line.moveTo(pointAt(0).x, pointAt(0).y)
        for (i in 1 until cleanValues.size) {
            line.lineTo(pointAt(i).x, pointAt(i).y)
        }
        area.addPath(line)
        area.lineTo(pointAt(cleanValues.lastIndex).x, padTop + plotH)
        area.lineTo(pointAt(0).x, padTop + plotH)
        area.close()

        // Progressive wipe: clip to the left fraction of the plot.
        val wipeRight = w * reveal
        clipPath(
            path = Path().apply {
                addRect(Rect(0f, 0f, wipeRight, h))
            },
        ) {
            drawPath(
                path = area,
                color = extended.signal.copy(alpha = 0.14f),
            )
            drawPath(
                path = line,
                color = extended.signal,
                style = Stroke(width = 2.dp.toPx()),
            )
        }

        // The latest reading becomes visible once the wipe passes it.
        val last = pointAt(cleanValues.lastIndex)
        if (wipeRight >= last.x) {
            drawCircle(
                color = extended.signalContainer,
                radius = 7.dp.toPx(),
                center = last,
            )
            drawCircle(
                color = extended.signal,
                radius = 4.dp.toPx(),
                center = last,
            )
        }

        scrubX?.let { x ->
            val index = (x / stepX).roundToInt().coerceIn(0, cleanValues.lastIndex)
            val point = pointAt(index)
            drawScrubReadout(
                x = point.x,
                y = point.y,
                value = cleanValues[index],
                plotTop = padTop,
                plotBottom = padTop + plotH,
                palette = ScrubPalette(
                    gridLine = extended.gridLine,
                    signal = extended.signal,
                    signalContainer = extended.signalContainer,
                    ink = scrubInk,
                ),
                textMeasurer = textMeasurer,
            )
        }
    }
}

/** The scrub layer: a dashed hairline across the plot, the reading picked out
 *  in the same halo-dot pair the latest reading wears, and the value in mono
 *  above the plot — the instrument answering a touch with its own number. */
private data class ScrubPalette(
    val gridLine: Color,
    val signal: Color,
    val signalContainer: Color,
    val ink: Color,
)

private fun DrawScope.drawScrubReadout(
    x: Float,
    y: Float,
    value: Float,
    plotTop: Float,
    plotBottom: Float,
    palette: ScrubPalette,
    textMeasurer: TextMeasurer,
) {
    drawLine(
        color = palette.gridLine,
        start = Offset(x, plotTop),
        end = Offset(x, plotBottom),
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 10f)),
    )
    drawCircle(color = palette.signalContainer, radius = 7.dp.toPx(), center = Offset(x, y))
    drawCircle(color = palette.signal, radius = 4.dp.toPx(), center = Offset(x, y))
    val label = if (value % 1f == 0f) {
        value.roundToInt().toString()
    } else {
        String.format(Locale.US, "%.1f", value)
    }
    val layout = textMeasurer.measure(
        text = label,
        style = NexusTypography.TelemetryMicro.copy(color = palette.ink),
    )
    drawText(
        textLayoutResult = layout,
        topLeft = Offset(
            (x - layout.size.width / 2f).coerceIn(0f, size.width - layout.size.width),
            (plotTop - layout.size.height - 4.dp.toPx()).coerceAtLeast(0f),
        ),
    )
}
