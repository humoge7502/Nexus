package com.nexus.ui.charts

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.sp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

data class RadarAxis(
    val label: String,
    val value: Float,
)

private val nexusGradientStart = Color(0xFF4F8EF7)
private val nexusGradientEnd = Color(0xFF7B5EA7)
private val strokeColor = Color(0xFF4F8EF7)

private const val NUM_GRID_LEVELS = 4

@Composable
fun NexusSkillRadarChart(
    axes: List<RadarAxis>,
    modifier: Modifier = Modifier,
    animationDuration: Int = 800,
) {
    val targetProgress = if (axes.isNotEmpty()) 1f else 0f
    val animatedProgress by animateFloatAsState(
        targetValue = targetProgress,
        animationSpec = tween(durationMillis = animationDuration),
        label = "radarProgress",
    )
    val textMeasurer = rememberTextMeasurer()

    val outlineVariant = MaterialTheme.colorScheme.outlineVariant
    val onSurface = MaterialTheme.colorScheme.onSurface
    Box(modifier = modifier.aspectRatio(1f)) {
        Canvas(modifier = Modifier.aspectRatio(1f)) {
            if (axes.isEmpty()) return@Canvas

            val center = Offset(size.width / 2f, size.height / 2f)
            val radius = min(size.width, size.height) / 2f * 0.8f
            val angleStep = (2f * PI / axes.size).toFloat()

            drawGrid(center, radius, axes.size, angleStep, outlineVariant)
            drawAxes(center, radius, axes.size, angleStep, outlineVariant)
            drawDataFill(center, radius, axes, angleStep, animatedProgress)
            drawDataStroke(center, radius, axes, angleStep, animatedProgress)
            drawLabels(center, radius, axes, angleStep, textMeasurer, onSurface)
        }
    }
}

private fun DrawScope.drawLabels(
    center: Offset,
    radius: Float,
    axes: List<RadarAxis>,
    angleStep: Float,
    textMeasurer: androidx.compose.ui.text.TextMeasurer,
    labelColor: Color,
) {
    axes.forEachIndexed { i, axis ->
        val angle = -PI.toFloat() / 2f + i * angleStep
        val labelRadius = radius * 1.18f
        val x = center.x + cos(angle) * labelRadius
        val y = center.y + sin(angle) * labelRadius
        val textResult = textMeasurer.measure(
            text = axis.label,
            style = TextStyle(
                color = labelColor,
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
            ),
        )
        drawText(
            textLayoutResult = textResult,
            topLeft = Offset(x - textResult.size.width / 2f, y - textResult.size.height / 2f),
        )
    }
}

private fun DrawScope.drawGrid(
    center: Offset,
    radius: Float,
    axisCount: Int,
    angleStep: Float,
    color: Color,
) {
    for (level in 1..NUM_GRID_LEVELS) {
        val gridRadius = radius * level / NUM_GRID_LEVELS
        val path = Path()
        for (i in 0 until axisCount) {
            val angle = -PI.toFloat() / 2f + i * angleStep
            val x = center.x + cos(angle) * gridRadius
            val y = center.y + sin(angle) * gridRadius
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        path.close()
        drawPath(
            path = path,
            color = color,
            style = Stroke(width = 1f),
        )
    }

    for (i in 0 until axisCount) {
        val angle = -PI.toFloat() / 2f + i * angleStep
        val x = center.x + cos(angle) * radius
        val y = center.y + sin(angle) * radius
        drawLine(
            color = color,
            start = center,
            end = Offset(x, y),
            strokeWidth = 1f,
        )
    }
}

private fun DrawScope.drawAxes(
    center: Offset,
    radius: Float,
    axisCount: Int,
    angleStep: Float,
    color: Color,
) {
    for (i in 0 until axisCount) {
        val angle = -PI.toFloat() / 2f + i * angleStep
        val endX = center.x + cos(angle) * radius
        val endY = center.y + sin(angle) * radius
        drawLine(
            color = color,
            start = center,
            end = Offset(endX, endY),
            strokeWidth = 1.5f,
        )
    }
}

private fun DrawScope.drawDataFill(
    center: Offset,
    radius: Float,
    axes: List<RadarAxis>,
    angleStep: Float,
    progress: Float,
) {
    val path = Path()
    axes.forEachIndexed { i, axis ->
        val angle = -PI.toFloat() / 2f + i * angleStep
        val scaledRadius = radius * (axis.value / 100f) * progress
        val x = center.x + cos(angle) * scaledRadius
        val y = center.y + sin(angle) * scaledRadius
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    path.close()

    drawPath(
        path = path,
        brush = Brush.radialGradient(
            colors = listOf(
                nexusGradientStart.copy(alpha = 0.5f),
                nexusGradientEnd.copy(alpha = 0.2f),
            ),
            center = center,
            radius = radius,
        ),
    )
}

private fun DrawScope.drawDataStroke(
    center: Offset,
    radius: Float,
    axes: List<RadarAxis>,
    angleStep: Float,
    progress: Float,
) {
    val path = Path()
    axes.forEachIndexed { i, axis ->
        val angle = -PI.toFloat() / 2f + i * angleStep
        val scaledRadius = radius * (axis.value / 100f) * progress
        val x = center.x + cos(angle) * scaledRadius
        val y = center.y + sin(angle) * scaledRadius
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    path.close()

    drawPath(
        path = path,
        color = strokeColor,
        style = Stroke(
            width = 2.5f,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round,
        ),
    )

    axes.forEachIndexed { i, axis ->
        val angle = -PI.toFloat() / 2f + i * angleStep
        val scaledRadius = radius * (axis.value / 100f) * progress
        val x = center.x + cos(angle) * scaledRadius
        val y = center.y + sin(angle) * scaledRadius
        drawCircle(
            color = strokeColor,
            radius = 4f,
            center = Offset(x, y),
        )
    }
}
