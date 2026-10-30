package com.nexus.designsystem.component

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.addOutline
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import com.nexus.designsystem.theme.LocalReducedMotion
import com.nexus.designsystem.theme.NexusAnimation
import com.nexus.designsystem.theme.NexusShapes
import com.nexus.designsystem.theme.nexus

/**
 * Surface effects, used sparingly.
 *
 *  pressScale()  — the surface acknowledges touch in 90ms, then releases
 *  innerLight()  — a vertical edge-light that traces any shape's outline
 *  sheen()       — one slow diagonal sweep across a signature gradient
 *  hairline()    — a 1dp stroke that lifts a surface off the ground
 *  arcGlow()     — a soft halo that makes a progress arc read as lit
 *  instrumentGrid() — the graticule, on one signature surface per screen
 *
 * Everything here collapses to a static state when the system asks for
 * reduced motion.
 */

/** Scales the surface while the finger is down. Works with any clickable
 *  further down the chain — no interaction-source wiring needed. */
fun Modifier.pressScale(pressedScale: Float = 0.97f): Modifier = composed {
    var pressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (pressed) pressedScale else 1f,
        animationSpec = tween(
            durationMillis = if (LocalReducedMotion.current) {
                0
            } else {
                NexusAnimation.Duration.micro
            },
            easing = NexusAnimation.Easing.standard,
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

/** A 1dp light stroke just inside a shape's outline, fading downward.
 *  This is what makes a card read as a milled surface rather than a filled
 *  rectangle — it follows any [shape], including cut corners. */
fun Modifier.innerLight(
    shape: Shape = NexusShapes.md,
    color: Color? = null,
): Modifier = composed {
    val resolvedColor = color ?: MaterialTheme.nexus.innerLight
    val density = LocalDensity.current
    val layoutDirection = LocalLayoutDirection.current
    val strokePx = with(density) { 1.dp.toPx() }
    this.drawWithContent {
        drawContent()
        val inset = strokePx / 2f
        if (size.width <= strokePx || size.height <= strokePx) return@drawWithContent
        val outline = shape.createOutline(
            size = Size(size.width - strokePx, size.height - strokePx),
            layoutDirection = layoutDirection,
            density = density,
        )
        val path = Path().apply { addOutline(outline) }
        translate(left = inset, top = inset) {
            drawPath(
                path = path,
                brush = Brush.verticalGradient(
                    colors = listOf(resolvedColor, Color.Transparent),
                    startY = 0f,
                    endY = size.height * 0.6f,
                ),
                style = Stroke(width = strokePx),
            )
        }
    }
}

/** A slow diagonal light sweep for signature gradient surfaces. Keep alpha
 *  low — it should read as a surface catching light, never as an effect. */
fun Modifier.sheen(
    periodMs: Int = 4200,
    alpha: Float = 0.10f,
): Modifier = composed {
    if (LocalReducedMotion.current) return@composed this
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

/** A 1dp stroke on the surface edge. Defaults to the theme's strong hairline
 *  so it stays visible on plum ink and on warm paper alike. */
fun Modifier.hairline(
    color: Color? = null,
    cornerRadiusDp: Float = 24f,
): Modifier = composed {
    val resolvedColor = color ?: MaterialTheme.nexus.hairlineStrong
    val stroke = with(LocalDensity.current) { 1.dp.toPx() }
    val radius = with(LocalDensity.current) { cornerRadiusDp.dp.toPx() }
    this.drawWithContent {
        drawContent()
        drawRoundRect(
            color = resolvedColor,
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

/** Faint vertical rules across a surface, like the graticule on a screen.
 *  Use on one signature surface per screen; keep [alpha] at or below 0.06. */
fun Modifier.instrumentGrid(
    spacingDp: Float = 26f,
    alpha: Float = 0.045f,
    color: Color? = null,
): Modifier = composed {
    val resolvedColor = color ?: MaterialTheme.colorScheme.onSurface
    val spacing = with(LocalDensity.current) { spacingDp.dp.toPx() }
    drawWithContent {
        drawContent()
        var x = spacing
        while (x < size.width) {
            drawLine(
                color = resolvedColor.copy(alpha = alpha),
                start = Offset(x, 0f),
                end = Offset(x, size.height),
                strokeWidth = 1f,
            )
            x += spacing
        }
    }
}
