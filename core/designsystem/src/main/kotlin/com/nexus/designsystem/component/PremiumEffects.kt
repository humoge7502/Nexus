package com.nexus.designsystem.component

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp

/**
 * Premium surface effects — the Compose-native equivalents of Magic UI /
 * React Bits signature moves, as reusable modifiers.
 *
 *  pressScale()  — every tappable card visibly reacts to touch (spring scale)
 *  sheen()       — a slow diagonal light sweep across gradient surfaces
 *  hairline()    — a 1dp light stroke that lifts gradient cards off the
 *                  background
 *  arcGlow()     — a soft multi-layer halo that makes progress rings feel lit
 */

/** Spring-scales the surface while the finger is down. Works with any
 *  clickable further down the chain — no interaction-source wiring needed. */
fun Modifier.pressScale(pressedScale: Float = 0.96f): Modifier = composed {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (pressed) pressedScale else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow,
        ),
        label = "pressScale",
    )
    this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .pointerInput(Unit) {
            awaitEachGesture {
                awaitFirstDown(requireUnconsumed = false)
                pressed = true
                waitForUpOrCancellation()
                pressed = false
            }
        }
}

/** A slow diagonal light sweep for gradient hero surfaces. Keep alpha low —
 *  it should read as glass catching light, never as an effect. */
fun Modifier.sheen(
    periodMs: Int = 4200,
    alpha: Float = 0.10f,
): Modifier = composed {
    val transition = rememberInfiniteTransition(label = "sheen")
    val progress by transition.animateFloat(
        initialValue = -0.5f,
        targetValue = 1.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = periodMs, easing = LinearEasing),
        ),
        label = "sheenProgress",
    )
    this.drawWithContent {
        drawContent()
        val w = size.width
        val x = w * progress
        drawRect(
            brush = Brush.linearGradient(
                colors = listOf(
                    Color.Transparent,
                    Color.White.copy(alpha = alpha),
                    Color.Transparent,
                ),
                start = Offset(x - w * 0.28f, 0f),
                end = Offset(x + w * 0.28f, size.height),
            ),
        )
    }
}

/** A 1dp light stroke just inside the surface edge — the single highest-
 *  leverage detail on gradient cards. [cornerRadiusPx] in pixels. */
fun Modifier.hairline(
    color: Color = Color.White.copy(alpha = 0.22f),
    cornerRadiusDp: Float = 24f,
): Modifier = composed {
    val stroke = with(LocalDensity.current) { 1.5.dp.toPx() }
    val radius = with(LocalDensity.current) { cornerRadiusDp.dp.toPx() }
    this.drawWithContent {
        drawContent()
        drawRoundRect(
            color = color,
            style = Stroke(width = stroke),
            cornerRadius = CornerRadius(radius),
        )
    }
}

/** A soft multi-layer halo behind an arc — makes progress rings feel lit
 *  rather than flat. Draw before the solid arc. */
fun DrawScope.arcGlow(
    color: Color,
    startAngle: Float,
    sweepAngle: Float,
    baseWidthPx: Float,
) {
    listOf(2.4f to 0.10f, 1.7f to 0.16f, 1.2f to 0.25f).forEach { (mult, alpha) ->
        drawArc(
            color = color.copy(alpha = alpha),
            startAngle = startAngle,
            sweepAngle = sweepAngle,
            useCenter = false,
            style = Stroke(width = baseWidthPx * mult, cap = StrokeCap.Round),
        )
    }
}
