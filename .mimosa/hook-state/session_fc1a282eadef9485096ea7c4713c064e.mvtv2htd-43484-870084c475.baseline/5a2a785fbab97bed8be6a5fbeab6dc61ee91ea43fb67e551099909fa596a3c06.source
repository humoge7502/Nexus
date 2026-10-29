package com.nexus.ui.charts

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.dp
import com.nexus.ui.R
import kotlin.math.roundToInt

private val nexusPrimary = Color(0xFF4F8EF7)
private val nexusSecondary = Color(0xFF06B6D4)
private val nexusAccent = Color(0xFF06B6D4)

@Composable
fun NexusLineChart(
    data: List<Float>,
    labels: List<String> = emptyList(),
    lineColor: Color = nexusPrimary,
    modifier: Modifier = Modifier,
) {
    val maxVal = remember(data) { data.maxOrNull()?.coerceAtLeast(1f) ?: 1f }
    val description = if (data.isEmpty()) {
        stringResource(R.string.cd_line_chart, 0, 0f, 0f)
    } else {
        stringResource(R.string.cd_line_chart, data.size, data.last(), data.max())
    }
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
            .clearAndSetSemantics { contentDescription = description }
    ) {
        if (data.isEmpty()) return@Canvas
        val stepX = size.width / (data.size - 1).coerceAtLeast(1)
        val path = Path()
        data.forEachIndexed { i, v ->
            val x = i * stepX
            val y = size.height - (v / maxVal) * size.height * 0.9f
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        drawPath(path, color = lineColor, style = Stroke(width = 3f, cap = StrokeCap.Round))
        data.forEachIndexed { i, v ->
            val x = i * stepX
            val y = size.height - (v / maxVal) * size.height * 0.9f
            drawCircle(lineColor, radius = 4f, center = Offset(x, y))
        }
    }
}

@Composable
fun NexusBarChart(
    data: List<Float>,
    barColor: Color = nexusPrimary,
    modifier: Modifier = Modifier,
) {
    val maxVal = remember(data) { data.maxOrNull()?.coerceAtLeast(1f) ?: 1f }
    val barDescription = if (data.isEmpty()) {
        stringResource(R.string.cd_bar_chart, 0, 0f)
    } else {
        stringResource(R.string.cd_bar_chart, data.size, data.max())
    }
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
            .clearAndSetSemantics { contentDescription = barDescription }
    ) {
        if (data.isEmpty()) return@Canvas
        val barWidth = size.width / data.size * 0.6f
        val gap = size.width / data.size * 0.4f
        data.forEachIndexed { i, v ->
            val barHeight = (v / maxVal) * size.height * 0.9f
            val x = i * (barWidth + gap) + gap / 2
            val y = size.height - barHeight
            drawRoundRect(
                color = barColor,
                topLeft = Offset(x, y),
                size = Size(barWidth, barHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(4f, 4f),
            )
        }
    }
}

data class DonutSlice(
    val label: String,
    val value: Float,
    val color: Color,
)

@Composable
fun NexusDonutChart(
    slices: List<DonutSlice>,
    modifier: Modifier = Modifier,
    strokeWidth: Float = 32f,
    centerLabel: String = "",
) {
    val total = slices.sumOf { it.value.toDouble() }.toFloat()
    val colors = listOf(nexusPrimary, nexusSecondary, nexusAccent,
        Color(0xFFF59E0B), Color(0xFF10B981), Color(0xFFEC4899))
    val sliceDescriptions = slices.map { slice ->
        stringResource(R.string.chart_slice_label, slice.label, percentOf(slice.value, total))
    }
    val description = stringResource(R.string.cd_donut_chart, sliceDescriptions.joinToString(", "))

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clearAndSetSemantics { contentDescription = description },
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.size(200.dp)) {
            val stroke = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
            var startAngle = -90f
            slices.forEachIndexed { index, slice ->
                val sweep = if (total > 0f) (slice.value / total) * 360f else 0f
                drawArc(
                    color = if (index < colors.size) colors[index] else slice.color,
                    startAngle = startAngle,
                    sweepAngle = sweep,
                    useCenter = false,
                    topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f),
                    size = Size(size.width - strokeWidth, size.height - strokeWidth),
                    style = stroke,
                )
                startAngle += sweep
            }
        }
        if (centerLabel.isNotEmpty()) {
            Text(
                text = centerLabel,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
    Spacer(modifier = Modifier.height(12.dp))
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        slices.forEach { slice ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size(10.dp),
                    shape = RoundedCornerShape(2.dp),
                    color = slice.color,
                ) {}
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = stringResource(
                        R.string.chart_slice_label,
                        slice.label,
                        percentOf(slice.value, total),
                    ),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

private fun percentOf(value: Float, total: Float): Int =
    if (total <= 0f) 0 else ((value / total) * 100).roundToInt()
