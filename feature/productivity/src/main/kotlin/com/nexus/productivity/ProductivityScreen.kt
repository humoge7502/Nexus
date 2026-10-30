package com.nexus.productivity

import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import com.nexus.productivity.R

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nexus.designsystem.component.NexusCard
import com.nexus.designsystem.component.NexusTopBar
import com.nexus.designsystem.theme.nexus
import kotlinx.coroutines.delay

@Composable
fun ProductivityScreen(
    onBack: () -> Unit,
) {
    var isRunning by remember { mutableStateOf(false) }
    var secondsRemaining by remember { mutableStateOf(25 * 60) }
    var pomodorosCompleted by remember { mutableStateOf(0) }

    val progress = 1f - (secondsRemaining.toFloat() / (25 * 60))
    val formattedTime = "${(secondsRemaining / 60).toString().padStart(2, '0')}:${(secondsRemaining % 60).toString().padStart(2, '0')}"
    val trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
    val accent = MaterialTheme.colorScheme.primary
    val accentTail = MaterialTheme.nexus.structure
    val dotCore = MaterialTheme.colorScheme.surfaceContainerLow

    LaunchedEffect(isRunning) {
        if (!isRunning) return@LaunchedEffect
        while (secondsRemaining > 0) {
            delay(1000L)
            secondsRemaining--
        }
        if (secondsRemaining == 0) {
            pomodorosCompleted++
            secondsRemaining = 25 * 60
            isRunning = false
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        NexusTopBar(
            title = stringResource(R.string.productivity_focus),
            onBack = onBack,
        )

        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(260.dp)
                    .semantics {
                        progressBarRangeInfo = ProgressBarRangeInfo(progress, 0f..1f)
                    },
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeWidth = 16.dp.toPx()
                    val arcSize = size.copy(
                        width = size.width - strokeWidth,
                        height = size.height - strokeWidth
                    )
                    val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)

                    drawArc(
                        color = trackColor,
                        startAngle = -90f, sweepAngle = 360f,
                        useCenter = false,
                        style = Stroke(strokeWidth, cap = StrokeCap.Round),
                        size = arcSize, topLeft = topLeft
                    )

                    drawArc(
                        brush = Brush.sweepGradient(
                            listOf(accent.copy(alpha = 0.55f), accent, accentTail),
                            center = center
                        ),
                        startAngle = -90f, sweepAngle = 360f * progress,
                        useCenter = false,
                        style = Stroke(strokeWidth, cap = StrokeCap.Round),
                        size = arcSize, topLeft = topLeft
                    )

                    if (progress > 0.02f) {
                        val angle = java.lang.Math.toRadians((-90 + 360 * progress).toDouble())
                        val r = (size.minDimension - strokeWidth) / 2
                        val dotCenter = Offset(
                            center.x + r * kotlin.math.cos(angle).toFloat(),
                            center.y + r * kotlin.math.sin(angle).toFloat()
                        )
                        drawCircle(color = accent, radius = strokeWidth / 2 + 2.dp.toPx(), center = dotCenter)
                        drawCircle(color = dotCore, radius = strokeWidth / 4, center = dotCenter)
                    }
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = formattedTime,
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = stringResource(R.string.productivity_focus),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                IconButton(
                    onClick = { isRunning = false; secondsRemaining = 25 * 60 },
                    modifier = Modifier.size(48.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceContainerHighest)
                ) {
                    Icon(
                        Icons.Default.Replay,
                        contentDescription = stringResource(R.string.cd_reset),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                Button(
                    onClick = { isRunning = !isRunning },
                    modifier = Modifier.size(72.dp),
                    shape = CircleShape,
                ) {
                    Icon(
                        imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = stringResource(
                            if (isRunning) R.string.productivity_pause else R.string.productivity_start,
                        ),
                        modifier = Modifier.size(32.dp),
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }

                IconButton(
                    onClick = { pomodorosCompleted++ },
                    modifier = Modifier.size(48.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceContainerHighest)
                ) {
                    Icon(
                        Icons.Default.SkipNext,
                        contentDescription = stringResource(R.string.cd_skip),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            NexusCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        stringResource(R.string.productivity_today_s_progress),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        TodayStat(
                            value = stringResource(
                                R.string.productivity_pomodoroscompleted,
                                pomodorosCompleted,
                            ),
                            label = stringResource(R.string.productivity_pomodoros),
                            color = MaterialTheme.colorScheme.primary,
                        )
                        TodayStat(
                            value = stringResource(
                                R.string.productivity_label,
                                pomodorosCompleted * 25,
                            ),
                            label = stringResource(R.string.productivity_minutes),
                            color = MaterialTheme.nexus.positive,
                        )
                        TodayStat(
                            value = stringResource(R.string.productivity_12),
                            label = stringResource(R.string.productivity_streak),
                            color = MaterialTheme.nexus.attention,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TodayStat(value: String, label: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
            color = color,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
