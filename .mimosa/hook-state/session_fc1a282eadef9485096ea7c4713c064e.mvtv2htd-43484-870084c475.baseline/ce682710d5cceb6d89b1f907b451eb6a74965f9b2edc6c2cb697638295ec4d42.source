@file:Suppress("NOTHING_TO_INLINE")

package com.nexus.designsystem.effect

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.MaterialTheme
import kotlinx.coroutines.delay
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * ScorePulseAnimation — the iconic NEXUS visual moment.
 *
 * Triggered when [currentScore] differs from [previousScore].
 * Shows an animated circular progress ring with:
 *   - Smooth arc animation from old position to new
 *   - Expanding pulse ring (color: green for up, rose for down)
 *   - Haptic feedback on score change
 *   - Optional 2-second overlay with change explanation
 *
 * Usage:
 * ```
 * var score by remember { mutableIntStateOf(500) }
 * var prevScore by remember { mutableIntStateOf(480) }
 * ScorePulseAnimation(score = score, previousScore = prevScore)
 * ```
 */
@Composable
fun ScorePulseAnimation(
    currentScore: Int,
    previousScore: Int,
    modifier: Modifier = Modifier,
    size: Dp = 140.dp,
    strokeWidth: Dp = 10.dp,
    overlayText: String? = null,
    onOverlayComplete: (() -> Unit)? = null,
) {
    val delta = currentScore - previousScore
    val progress = (currentScore / 1000f).coerceIn(0f, 1f)
    val prevProgressFloat = (previousScore / 1000f).coerceIn(0f, 1f)

    val animatedProgress = remember { Animatable(prevProgressFloat) }
    val pulseScale = remember { Animatable(0f) }
    val pulseAlpha = remember { Animatable(0f) }

    val haptic = LocalHapticFeedback.current
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant

    LaunchedEffect(currentScore, previousScore) {
        if (delta == 0) return@LaunchedEffect
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)

        animatedProgress.animateTo(
            targetValue = progress,
            animationSpec = tween(durationMillis = 900, easing = { it * it * (3 - 2 * it) }),
        )

        pulseScale.snapTo(0.6f)
        pulseAlpha.snapTo(0.9f)
        pulseScale.animateTo(1.5f, tween(400))
        pulseAlpha.animateTo(0f, tween(350))
    }

    Box(modifier = modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(size)) {
            val stroke = strokeWidth.toPx()
            val radius = (min(size.toPx(), size.toPx()) - stroke * 2) / 2f
            val center = Offset(size.toPx() / 2f, size.toPx() / 2f)

            drawArc(
                color = surfaceVariant,
                startAngle = -90f, sweepAngle = 360f, useCenter = false,
                style = Stroke(width = stroke, cap = StrokeCap.Round),
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2, radius * 2),
            )
            drawArc(
                brush = androidx.compose.ui.graphics.Brush.sweepGradient(
                    colors = listOf(Color(0xFF4F8EF7), Color(0xFF06B6D4)),
                    center = center,
                ),
                startAngle = -90f, sweepAngle = 360f * progress, useCenter = false,
                style = Stroke(width = stroke, cap = StrokeCap.Round),
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2, radius * 2),
            )
        }
        androidx.compose.material3.Text(
            text = currentScore.toString(),
            style = TextStyle(fontSize = 32.sp, fontWeight = FontWeight.ExtraBold),
            color = Color(0xFF4F8EF7),
        )
    }
}
