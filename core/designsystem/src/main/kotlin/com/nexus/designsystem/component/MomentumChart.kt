package com.nexus.designsystem.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.unit.dp
import com.nexus.designsystem.theme.NexusAnimation
import com.nexus.designsystem.theme.motionMillis
import com.nexus.designsystem.theme.nexus

/**
 * The career-momentum instrument: the user's Ascent-score history drawn as one
 * progressive line with a soft area under it, in the signal color.
 *
 * Data contract: [values] are chronological score readings (any scale; the
 * chart normalises to its own min/max window). Fewer than two readings render
 * the baseline only — the chart never invents history. The reveal is a single
 * left-to-right wipe driven by [NexusAnimation]; with reduced motion it
 * appears fully drawn immediately.
 */
@Composable
fun MomentumChart(
    values: List<Float>,
    modifier: Modifier = Modifier,
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

    val cleanValues = remember(values) { values.filter { it.isFinite() } }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(120.dp),
    ) {
        val w = size.width
        val h = size.height
        val padTop = h * 0.10f
        val padBottom = h * 0.12f
        val plotH = h - padTop - padBottom

        // Baseline grid: dashed hairlines, per the instrument grid language.
        val gridEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(6f, 10f))
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
    }
}
