package com.nexus.designsystem.chart

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Box
import com.nexus.designsystem.R

@Composable
fun CareerScoreRing(
    score: Int,
    modifier: Modifier = Modifier,
    maxScore: Int = 1000,
    size: Int = 180,
) {
    val animatedProgress by animateFloatAsState(
        targetValue = score.toFloat() / maxScore,
        animationSpec = tween(durationMillis = 1000),
        label = "scoreProgress",
    )
    val backgroundColor = MaterialTheme.colorScheme.surfaceVariant
    val description = stringResource(R.string.cd_score_ring, score, maxScore)
    Box(
        modifier = modifier
            .size(size.dp)
            .clearAndSetSemantics {
                contentDescription = description
                progressBarRangeInfo = ProgressBarRangeInfo(
                    current = score.toFloat(),
                    range = 0f..maxScore.toFloat(),
                )
            },
        contentAlignment = Alignment.Center,
    ) {
        Canvas(modifier = Modifier.size(size.dp)) {
            val strokeWidth = 16.dp.toPx()
            val canvasSize = this.size
            val topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f)
            val arcSize = Size(canvasSize.width - strokeWidth, canvasSize.height - strokeWidth)
            drawArc(
                color = backgroundColor,
                startAngle = -90f, sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft, size = arcSize,
                style = Stroke(strokeWidth, cap = StrokeCap.Round),
            )
            val gradient = Brush.sweepGradient(
                colors = listOf(Color(0xFF4F8EF7), Color(0xFF06B6D4), Color(0xFF00D9A3)),
            )
            drawArc(
                brush = gradient,
                startAngle = -90f, sweepAngle = 360f * animatedProgress,
                useCenter = false,
                topLeft = topLeft, size = arcSize,
                style = Stroke(strokeWidth, cap = StrokeCap.Round),
            )
        }
        Text(
            text = "$score",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}
