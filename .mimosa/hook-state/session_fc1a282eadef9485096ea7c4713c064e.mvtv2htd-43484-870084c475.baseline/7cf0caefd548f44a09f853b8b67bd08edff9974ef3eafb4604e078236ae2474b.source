package com.nexus.designsystem.component

import androidx.compose.foundation.Canvas
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * The quiet ground layer: one azure bloom at the top of the screen and a set of
 * vertical hairlines that fade out beneath it, like signal columns on
 * engineering paper. Static and single-pass — it costs one Canvas draw, never a
 * per-frame allocation — and stays below 5% alpha so it reads as depth rather
 * than decoration.
 */
@Composable
fun SignalField(
    modifier: Modifier = Modifier,
    accent: Color = MaterialTheme.colorScheme.primary,
    bloomHeight: Dp = 420.dp,
    columnSpacing: Dp = 44.dp,
) {
    val onSurface = MaterialTheme.colorScheme.onSurface
    Canvas(modifier = modifier) {
        val bloomPx = bloomHeight.toPx()
        drawRect(
            brush = Brush.radialGradient(
                colors = listOf(accent.copy(alpha = 0.16f), Color.Transparent),
                center = Offset(size.width * 0.5f, -size.width * 0.15f),
                radius = size.width * 0.95f,
            ),
            size = androidx.compose.ui.geometry.Size(size.width, bloomPx),
        )

        val columnBrush = Brush.verticalGradient(
            colors = listOf(onSurface.copy(alpha = 0.045f), Color.Transparent),
            startY = 0f,
            endY = bloomPx,
        )
        val spacing = columnSpacing.toPx()
        var x = spacing
        while (x < size.width) {
            drawLine(
                brush = columnBrush,
                start = Offset(x, 0f),
                end = Offset(x, bloomPx),
                strokeWidth = 1f,
            )
            x += spacing
        }
    }
}
