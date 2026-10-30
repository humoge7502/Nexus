package com.nexus.designsystem.component

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.nexus.designsystem.theme.NexusColors
import kotlin.math.cos
import kotlin.math.sin

/**
 * An open 270° dial: a graduated tick ring, a lit progress arc and a terminal
 * bead. Ticks step at four degrees with every sixth graduating longer, so the
 * scale reads as calibrated rather than decorative, and the open bottom keeps
 * it from looking like a generic donut chart.
 *
 * The sweep runs once on first composition; afterwards the arc animates to any
 * new value. Compose honours the system animator scale, so reduced-motion
 * settings collapse it automatically.
 */
@Composable
fun InstrumentGauge(
    progress: Float,
    modifier: Modifier = Modifier,
    accent: Color = NexusColors.Cyan400,
    trackColor: Color = Color.White.copy(alpha = 0.16f),
    tickColor: Color = Color.White.copy(alpha = 0.30f),
    stroke: Dp = 9.dp,
    content: @Composable BoxScope.() -> Unit = {},
) {
    val target = progress.coerceIn(0f, 1f)
    var started by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { started = true }
    val value by animateFloatAsState(
        targetValue = if (started) target else 0f,
        animationSpec = tween(durationMillis = 1400, easing = FastOutSlowInEasing),
        label = "gaugeSweep",
    )

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val outer = size.minDimension / 2f
            val arcStroke = stroke.toPx()
            val arcRadius = outer - 20.dp.toPx()
            val centre = Offset(size.width / 2f, size.height / 2f)
            val start = 135f
            val sweep = 270f

            drawArc(
                color = trackColor,
                startAngle = start,
                sweepAngle = sweep,
                useCenter = false,
                topLeft = Offset(centre.x - arcRadius, centre.y - arcRadius),
                size = androidx.compose.ui.geometry.Size(arcRadius * 2, arcRadius * 2),
                style = Stroke(width = arcStroke, cap = StrokeCap.Round),
            )

            val tickCount = 68
            repeat(tickCount) { i ->
                val angle = Math.toRadians((start + sweep * i / (tickCount - 1)).toDouble()).toFloat()
                val major = i % 6 == 0
                val length = if (major) 7.dp.toPx() else 4.dp.toPx()
                val w = if (major) 1.4.dp.toPx() else 1.dp.toPx()
                val from = outer
                val to = outer - length
                drawLine(
                    color = tickColor,
                    start = Offset(centre.x + cos(angle) * from, centre.y + sin(angle) * from),
                    end = Offset(centre.x + cos(angle) * to, centre.y + sin(angle) * to),
                    strokeWidth = w,
                    cap = StrokeCap.Round,
                )
            }

            val litSweep = sweep * value
            arcGlow(
                color = accent,
                startAngle = start,
                sweepAngle = litSweep,
                baseWidthPx = arcStroke,
            )
            drawArc(
                color = accent,
                startAngle = start,
                sweepAngle = litSweep,
                useCenter = false,
                topLeft = Offset(centre.x - arcRadius, centre.y - arcRadius),
                size = androidx.compose.ui.geometry.Size(arcRadius * 2, arcRadius * 2),
                style = Stroke(width = arcStroke, cap = StrokeCap.Round),
            )

            val head = Math.toRadians((start + litSweep).toDouble()).toFloat()
            val headCentre = Offset(
                centre.x + cos(head) * arcRadius,
                centre.y + sin(head) * arcRadius,
            )
            drawCircle(color = accent.copy(alpha = 0.22f), radius = arcStroke * 1.5f, center = headCentre)
            drawCircle(color = Color.White, radius = arcStroke * 0.55f, center = headCentre)
        }
        content()
    }
}
