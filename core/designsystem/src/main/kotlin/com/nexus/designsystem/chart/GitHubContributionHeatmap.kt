package com.nexus.designsystem.chart

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

data class ContributionDay(val date: String, val count: Int)
data class ContributionWeek(val days: List<ContributionDay>)

@Composable
fun GitHubContributionHeatmap(
    weeks: List<ContributionWeek>,
    modifier: Modifier = Modifier,
) {
    val cellSize = 11.dp
    val cellSpacing = 3.dp
    val totalCellSize = cellSize + cellSpacing
    val greenColor = Color(0xFF00D9A3)

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(totalCellSize * 7),
    ) {
        weeks.forEachIndexed { weekIndex, week ->
            week.days.forEachIndexed { dayIndex, day ->
                val x = weekIndex * totalCellSize.toPx()
                val y = dayIndex * totalCellSize.toPx()
                val alpha = when {
                    day.count == 0 -> 0.12f
                    day.count <= 3 -> 0.35f
                    day.count <= 7 -> 0.55f
                    day.count <= 12 -> 0.75f
                    else -> 1.0f
                }
                drawRoundRect(
                    color = greenColor.copy(alpha = alpha),
                    topLeft = Offset(x, y),
                    size = Size(cellSize.toPx(), cellSize.toPx()),
                    cornerRadius = CornerRadius(2.dp.toPx()),
                )
            }
        }
    }
}
