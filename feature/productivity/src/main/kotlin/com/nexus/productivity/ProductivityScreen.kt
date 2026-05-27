package com.nexus.productivity

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import com.nexus.designsystem.theme.NexusColors
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductivityScreen(
    onBack: () -> Unit,
) {
    var isRunning by remember { mutableStateOf(false) }
    var secondsRemaining by remember { mutableStateOf(25 * 60) }
    var sessionType by remember { mutableStateOf("Focus") }
    var pomodorosCompleted by remember { mutableStateOf(0) }

    val progress = 1f - (secondsRemaining.toFloat() / (25 * 60))
    val formattedTime = "${(secondsRemaining / 60).toString().padStart(2, '0')}:${(secondsRemaining % 60).toString().padStart(2, '0')}"

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
        TopAppBar(
            title = { Text("Focus", fontWeight = FontWeight.Bold) },
            navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, contentDescription = "Back") } },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
        )

        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(260.dp)) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeWidth = 16.dp.toPx()
                    val arcSize = size.copy(
                        width = size.width - strokeWidth,
                        height = size.height - strokeWidth
                    )
                    val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)

                    drawArc(
                        color = Color(0xFF6366F1).copy(alpha = 0.1f),
                        startAngle = -90f, sweepAngle = 360f,
                        useCenter = false,
                        style = Stroke(strokeWidth, cap = StrokeCap.Round),
                        size = arcSize, topLeft = topLeft
                    )

                    drawArc(
                        brush = Brush.sweepGradient(
                            listOf(Color(0xFF6366F1).copy(alpha = 0.5f), Color(0xFF6366F1), Color(0xFF8B5CF6)),
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
                        drawCircle(color = Color(0xFF6366F1), radius = strokeWidth / 2 + 2.dp.toPx(), center = dotCenter)
                        drawCircle(color = Color.White, radius = strokeWidth / 4, center = dotCenter)
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
                        text = sessionType,
                        style = MaterialTheme.typography.labelLarge,
                        color = Color(0xFF6366F1)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                IconButton(
                    onClick = { isRunning = false; secondsRemaining = 25 * 60 },
                    modifier = Modifier.size(48.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Icon(Icons.Default.Replay, contentDescription = "Reset", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                Button(
                    onClick = { isRunning = !isRunning },
                    modifier = Modifier.size(72.dp),
                    shape = CircleShape,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1))
                ) {
                    Icon(
                        imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isRunning) "Pause" else "Start",
                        modifier = Modifier.size(32.dp),
                        tint = Color.White
                    )
                }

                IconButton(
                    onClick = { pomodorosCompleted++ },
                    modifier = Modifier.size(48.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Icon(Icons.Default.SkipNext, contentDescription = "Skip", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Spacer(modifier = Modifier.height(40.dp))

            NexusCard(modifier = Modifier.fillMaxWidth(), glassEffect = true) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Today's Progress", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("$pomodorosCompleted", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold, color = Color(0xFF6366F1))
                            Text("Pomodoros", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("${pomodorosCompleted * 25}", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                            Text("Minutes", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("12", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.Bold, color = Color(0xFFF59E0B))
                            Text("Streak", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}
