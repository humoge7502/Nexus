package com.nexus.ui.charts

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import com.nexus.ui.R

private val nexusBlue = Color(0xFF4F8EF7)

@Composable
fun NexusCareerScoreChart(
    scores: List<Float>,
    modifier: Modifier = Modifier,
) {
    val maxVal = remember(scores) { scores.maxOrNull()?.coerceAtLeast(1f) ?: 1f }
    val description = stringResource(
        R.string.cd_career_score_chart,
        scores.size,
        scores.lastOrNull() ?: 0f,
        scores.maxOrNull() ?: 0f,
    )
    Canvas(
        modifier = modifier.clearAndSetSemantics { contentDescription = description },
    ) {
        if (scores.isEmpty()) return@Canvas
        val stepX = size.width / (scores.size - 1).coerceAtLeast(1)
        val path = Path()
        scores.forEachIndexed { i, v ->
            val x = i * stepX
            val y = size.height - (v / maxVal) * size.height * 0.85f
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        drawPath(
            path = path,
            brush = Brush.verticalGradient(
                colors = listOf(nexusBlue, nexusBlue.copy(alpha = 0f)),
            ),
            style = Stroke(width = 3f, cap = StrokeCap.Round),
        )
        scores.forEachIndexed { i, v ->
            val x = i * stepX
            val y = size.height - (v / maxVal) * size.height * 0.85f
            drawCircle(nexusBlue, radius = 4f, center = Offset(x, y))
        }
    }
}
