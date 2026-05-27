package com.nexus.designsystem.effect

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

@Composable
fun MeshGradientBackground(
    modifier: Modifier = Modifier,
    colors: List<Color> = listOf(
        Color(0xFF6366F1).copy(alpha = 0.6f),
        Color(0xFF06B6D4).copy(alpha = 0.4f),
        Color(0xFF8B5CF6).copy(alpha = 0.3f),
    )
) {
    val infiniteTransition = rememberInfiniteTransition(label = "mesh")

    val offset1 by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "offset1"
    )

    val offset2 by infiniteTransition.animateFloat(
        initialValue = 1f, targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "offset2"
    )

    val offset3 by infiniteTransition.animateFloat(
        initialValue = 0.5f, targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "offset3"
    )

    Canvas(modifier = modifier) {
        val c1 = Offset(
            size.width * (0.2f + offset1 * 0.4f),
            size.height * (0.2f + offset3 * 0.3f)
        )
        val c2 = Offset(
            size.width * (0.7f - offset2 * 0.3f),
            size.height * (0.6f + offset1 * 0.2f)
        )
        val c3 = Offset(
            size.width * (0.5f + offset2 * 0.2f),
            size.height * (0.8f - offset3 * 0.2f)
        )

        drawCircle(
            brush = Brush.radialGradient(
                listOf(colors[0], colors[0].copy(alpha = 0f)),
                center = c1, radius = size.minDimension * 0.6f
            ),
            radius = size.minDimension * 0.6f,
            center = c1
        )

        drawCircle(
            brush = Brush.radialGradient(
                listOf(colors[1], colors[1].copy(alpha = 0f)),
                center = c2, radius = size.minDimension * 0.5f
            ),
            radius = size.minDimension * 0.5f,
            center = c2
        )

        drawCircle(
            brush = Brush.radialGradient(
                listOf(colors[2], colors[2].copy(alpha = 0f)),
                center = c3, radius = size.minDimension * 0.4f
            ),
            radius = size.minDimension * 0.4f,
            center = c3
        )
    }
}
