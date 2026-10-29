package com.nexus.ui.charts

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import com.nexus.ui.R

private val auroraGreen = Color(0xFF00D9A3)
private val todayHighlight = Color(0xFF4F8EF7)

@Composable
fun NexusWeeklyXPChart(
    dailyXP: List<Float>,
    modifier: Modifier = Modifier,
    currentDayIndex: Int = -1,
) {
    val maxVal = remember(dailyXP) { dailyXP.maxOrNull()?.coerceAtLeast(1f) ?: 1f }
    val todayXp = dailyXP.getOrNull(currentDayIndex) ?: 0f
    val bestXp = dailyXP.maxOrNull() ?: 0f
    val description = stringResource(R.string.cd_weekly_xp_chart, dailyXP.size, todayXp, bestXp)
    Canvas(
        modifier = modifier.clearAndSetSemantics { contentDescription = description },
    ) {
        if (dailyXP.isEmpty()) return@Canvas
        val barWidth = size.width / dailyXP.size * 0.65f
        val gap = size.width / dailyXP.size * 0.35f
        dailyXP.forEachIndexed { index, xp ->
            val barHeight = (xp / maxVal) * size.height * 0.85f
            val color = if (index == currentDayIndex) todayHighlight else auroraGreen
            val x = index * (barWidth + gap) + gap / 2
            val y = size.height - barHeight
            drawRoundRect(
                color = color,
                topLeft = Offset(x, y),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(4f, 4f),
            )
        }
    }
}
