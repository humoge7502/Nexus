package com.nexus.ui.charts

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color

data class DSACounts(
    val easySolved: Float,
    val easyTotal: Float,
    val mediumSolved: Float,
    val mediumTotal: Float,
    val hardSolved: Float,
    val hardTotal: Float,
)

private val easyColor = Color(0xFF00D9A3)
private val mediumColor = Color(0xFFFFB800)
private val hardColor = Color(0xFFFF4757)

@Composable
fun NexusDSAProgressChart(
    counts: DSACounts,
    modifier: Modifier = Modifier,
) {
    val maxTotal = maxOf(counts.easyTotal, counts.mediumTotal, counts.hardTotal, 1f)
    Canvas(modifier = modifier) {
        val barWidth = size.width / 6f * 0.7f
        val groupGap = size.width / 6f * 0.3f

        fun drawBar(x: Float, solved: Float, total: Float, color: Color) {
            val solvedHeight = (solved / maxTotal) * size.height * 0.85f
            val totalHeight = (total / maxTotal) * size.height * 0.85f
            drawRoundRect(
                color = color.copy(alpha = 0.3f),
                topLeft = Offset(x, size.height - totalHeight),
                size = Size(barWidth, totalHeight),
                cornerRadius = CornerRadius(3f, 3f),
            )
            drawRoundRect(
                color = color,
                topLeft = Offset(x, size.height - solvedHeight),
                size = Size(barWidth, solvedHeight),
                cornerRadius = CornerRadius(3f, 3f),
            )
        }

        drawBar(0f + groupGap, counts.easySolved, counts.easyTotal, easyColor)
        drawBar(barWidth + groupGap * 2, counts.mediumSolved, counts.mediumTotal, mediumColor)
        drawBar((barWidth + groupGap) * 2 + groupGap, counts.hardSolved, counts.hardTotal, hardColor)
    }
}
