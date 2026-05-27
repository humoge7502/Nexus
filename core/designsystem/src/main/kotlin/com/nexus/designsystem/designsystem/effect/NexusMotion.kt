package com.nexus.designsystem.effect

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.nexus.designsystem.theme.NexusAnimation
import com.nexus.designsystem.theme.NexusColors
import com.nexus.designsystem.theme.NexusGradients
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun NexusUnfoldAnimation(
    isActive: Boolean,
    content: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    var progress by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(isActive) {
        if (isActive) {
            progress = 0f
            val anim = spring<Float>(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMediumLow,
            )
            progress = 1f
        }
    }

    val scale by remember { mutableFloatStateOf(if (isActive) 1f else 0.8f) }
    val alpha by remember { mutableFloatStateOf(if (isActive) 1f else 0f) }

    Box(
        modifier = modifier
            .scale(scale)
            .alpha(alpha),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}

@Composable
fun AchievementSupernova(
    isActive: Boolean,
    modifier: Modifier = Modifier,
    particleCount: Int = 60,
    particleColors: List<Color> = listOf(
        NexusColors.StellarPink,
        NexusColors.PlasmaOrange,
        NexusColors.AuroraGreen,
        NexusColors.NexusBlue,
        Color(0xFFFBBF24),
    ),
) {
    val transition = rememberInfiniteTransition(label = "supernova")

    if (!isActive) return

    Box(modifier = modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cx = size.width / 2
            val cy = size.height / 2
            val maxRadius = size.minDimension * 0.8f

            for (i in 0 until particleCount) {
                val angle = (i.toFloat() / particleCount) * 360f
                val rad = Math.toRadians(angle.toDouble()).toFloat()
                val phase = i * 0.1f
                val t = (System.currentTimeMillis() % 2000) / 2000f

                val distance = maxRadius * (0.3f + 0.7f * ((t + phase) % 1f))
                val px = cx + cos(rad) * distance
                val py = cy + sin(rad) * distance

                val particleSize = (2f + 4f * (1f - (t + phase) % 1f)) * density
                val alpha = 1f - ((t + phase) % 1f)
                val color = particleColors[i % particleColors.size]

                drawCircle(
                    color = color.copy(alpha = alpha * 0.8f),
                    radius = particleSize,
                    center = Offset(px, py),
                )
            }
        }
    }
}

@Composable
fun IntelligencePulse(
    isThinking: Boolean,
    modifier: Modifier = Modifier,
) {
    val transition = rememberInfiniteTransition(label = "intelPulse")

    val pulseProgress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "pulse",
    )

    if (!isThinking) return

    Box(
        modifier = modifier.size(32.dp),
        contentAlignment = Alignment.Center,
    ) {
        repeat(3) { index ->
            val delay = index * 0.2f
            val adjustedProgress = ((pulseProgress - delay) % 1f).coerceAtLeast(0f)
            val scale = 1f + adjustedProgress * 2f
            val alpha = 1f - adjustedProgress

            Box(
                modifier = Modifier
                    .size(32.dp)
                    .scale(scale)
                    .alpha(alpha)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(NexusGradients.Nexus).let {
                            androidx.compose.ui.graphics.Color.Transparent
                        }
                    ),
            )
        }

        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(Brush.linearGradient(NexusGradients.Nexus)),
        )
    }
}

@Composable
fun StreakIgnition(
    streakCount: Int,
    isMilestone: Boolean = streakCount > 0 && streakCount % 7 == 0,
    modifier: Modifier = Modifier,
) {
    val transition = rememberInfiniteTransition(label = "streakFlame")

    val flameIntensity by transition.animateFloat(
        initialValue = 0.7f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "flameIntensity",
    )

    val flameHeight by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "flameHeight",
    )

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(if (isMilestone) 48.dp else 32.dp)) {
            val cx = size.width / 2
            val baseY = size.height * 0.85f
            val flameRadius = size.minDimension * 0.25f * flameIntensity
            val heightOffset = size.minDimension * 0.15f * flameHeight

            val flameColor = if (isMilestone) {
                Brush.verticalGradient(
                    listOf(Color(0xFFFF6B35), Color(0xFFFBBF24), Color(0xFFF72585))
                )
            } else {
                Brush.verticalGradient(
                    listOf(Color(0xFFFF8C42), Color(0xFFFBBF24))
                )
            }

            drawCircle(
                brush = flameColor,
                radius = flameRadius + heightOffset * 0.3f,
                center = Offset(cx, baseY - heightOffset),
            )

            if (isMilestone) {
                val glowRadius = flameRadius * 1.5f
                drawCircle(
                    color = Color(0xFFFF6B35).copy(alpha = 0.15f * flameIntensity),
                    radius = glowRadius,
                    center = Offset(cx, baseY - heightOffset * 0.5f),
                )
            }
        }
    }
}

@Composable
fun NexusReveal(
    isVisible: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    var revealProgress by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(isVisible) {
        if (isVisible) {
            revealProgress = 0f
            kotlinx.coroutines.delay(50)
            revealProgress = 1f
        }
    }

    val blurAlpha by remember { mutableFloatStateOf(if (isVisible) 1f else 0f) }

    Box(
        modifier = modifier
            .alpha(blurAlpha),
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}

@Composable
fun SwipeableCard(
    onSwipeLeft: () -> Unit = {},
    onSwipeRight: () -> Unit = {},
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    var offsetX by remember { mutableFloatStateOf(0f) }

    val threshold = with(LocalDensity.current) { 100.dp.toPx() }
    val rotation = (offsetX / threshold).coerceIn(-1f, 1f) * 5f

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .scale(1f - (kotlin.math.abs(offsetX) / (threshold * 4f)).coerceAtMost(0.05f))
        ) {
            content()
        }
    }
}

fun DrawScope.drawIntelligenceRing(
    progress: Float,
    center: Offset,
    radius: Float,
    color: Color,
    strokeWidth: Float = 4f,
) {
    val sweepAngle = progress * 360f
    drawArc(
        color = color.copy(alpha = 0.2f),
        startAngle = -90f,
        sweepAngle = 360f,
        useCenter = false,
        topLeft = Offset(center.x - radius, center.y - radius),
        size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
        style = Stroke(width = strokeWidth),
    )
    drawArc(
        color = color,
        startAngle = -90f,
        sweepAngle = sweepAngle,
        useCenter = false,
        topLeft = Offset(center.x - radius, center.y - radius),
        size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
        style = Stroke(width = strokeWidth),
    )
}
