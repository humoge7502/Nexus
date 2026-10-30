package com.nexus.designsystem.effect

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import com.nexus.designsystem.theme.LocalReducedMotion
import com.nexus.designsystem.theme.NexusAnimation
import com.nexus.designsystem.theme.nexus
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

/**
 * Entrance for a panel that "unfolds" into place.
 *
 * It unfolds from its top edge — the way a physical instrument cover lifts — rather
 * than scaling from the centre, which would make every card feel like a modal. The
 * spring is critically damped: content arriving is not an achievement.
 */
@Composable
fun NexusUnfoldAnimation(
    isActive: Boolean,
    content: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    val reduced = LocalReducedMotion.current
    val progress by animateFloatAsState(
        targetValue = if (isActive) 1f else 0f,
        animationSpec = if (reduced) snap() else NexusAnimation.Springs.card,
        label = "unfold",
    )

    Box(
        modifier = modifier.graphicsLayer {
            alpha = progress
            scaleX = 0.94f + 0.06f * progress
            scaleY = 0.88f + 0.12f * progress
            transformOrigin = TransformOrigin(0.5f, 0f)
        },
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}

/**
 * One-shot celebration for an unlocked achievement.
 *
 * This is the only place in the product where motion is allowed to be decorative —
 * and it is still finite: the burst runs once, ends, and leaves nothing running.
 */
@Composable
fun AchievementSupernova(
    isActive: Boolean,
    modifier: Modifier = Modifier,
    particleCount: Int = 60,
    particleColors: List<Color> = emptyList(),
) {
    val palette = particleColors.ifEmpty {
        val ext = MaterialTheme.nexus
        listOf(ext.signal, ext.attention, ext.positive, ext.structure, ext.bone)
    }
    val reduced = LocalReducedMotion.current
    val burst = remember { Animatable(0f) }

    LaunchedEffect(isActive, reduced) {
        if (!isActive) {
            burst.snapTo(0f)
            return@LaunchedEffect
        }
        if (reduced) {
            burst.snapTo(1f)
            return@LaunchedEffect
        }
        burst.snapTo(0f)
        burst.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = NexusAnimation.Duration.dramatic + 400,
                easing = LinearEasing,
            ),
        )
    }

    if (!isActive) return

    Box(modifier = modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cx = size.width / 2f
            val cy = size.height / 2f
            val maxRadius = size.minDimension * 0.8f
            val count = particleCount.coerceIn(1, 240)

            if (reduced) {
                // No motion: a still, quiet starburst reads as "achieved" without
                // anything moving on screen.
                drawCircle(
                    color = palette.first().copy(alpha = 0.5f),
                    radius = maxRadius * 0.42f,
                    center = Offset(cx, cy),
                    style = Stroke(width = 1.dp.toPx()),
                )
                drawCircle(
                    color = palette.first().copy(alpha = 0.22f),
                    radius = maxRadius * 0.7f,
                    center = Offset(cx, cy),
                    style = Stroke(width = 1.dp.toPx()),
                )
                return@Canvas
            }

            val t = burst.value
            val fade = (1f - t) * (1f - t)

            // Shockwave: a single thin ring that outruns the particles and dies first.
            val waveT = (t / 0.4f).coerceIn(0f, 1f)
            if (waveT < 1f) {
                drawCircle(
                    color = palette.first().copy(alpha = (1f - waveT) * 0.45f),
                    radius = maxRadius * (0.1f + 0.6f * waveT),
                    center = Offset(cx, cy),
                    style = Stroke(width = 1.dp.toPx()),
                )
            }

            for (i in 0 until count) {
                // Deterministic jitter: a stable starburst, not a new random shape
                // every time the effect is recomposed.
                val jitter = ((i * 37) % 100) / 100f
                val angle = (i.toFloat() / count) * 360f + jitter * 9f
                val rad = angle * PI.toFloat() / 180f
                val speed = 0.55f + 0.45f * (((i * 53) % 100) / 100f)
                val distance = maxRadius * speed * (0.12f + 0.88f * t)

                drawCircle(
                    color = palette[i % palette.size].copy(alpha = fade * 0.9f),
                    radius = (3f - 2f * t) * density,
                    center = Offset(
                        x = cx + cos(rad) * distance,
                        y = cy + sin(rad) * distance,
                    ),
                )
            }

            // The bead the burst came from, snuffed out by the expansion.
            drawCircle(
                color = palette.first().copy(alpha = (1f - t) * 0.9f),
                radius = (7f * (1f - t) + 1f) * density,
                center = Offset(cx, cy),
            )
        }
    }
}

/**
 * "Still processing" indicator: concentric rings leaving a steady core.
 *
 * The core stays lit so the control never looks dead between rings, and the rings
 * are hairlines rather than blobs — the same calibrated-line language as the gauges.
 */
@Composable
fun IntelligencePulse(
    isThinking: Boolean,
    modifier: Modifier = Modifier,
) {
    if (!isThinking) return

    val signal = MaterialTheme.nexus.signal
    val reduced = LocalReducedMotion.current
    val pulse = rememberPulseProgress(reduced)

    Box(
        modifier = modifier.size(32.dp),
        contentAlignment = Alignment.Center,
    ) {
        if (reduced) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .border(width = 1.dp, color = signal.copy(alpha = 0.45f), shape = CircleShape),
            )
        } else {
            repeat(3) { index ->
                val phase = (pulse + index / 3f) % 1f
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .scale(1f + phase * 1.3f)
                        .alpha((1f - phase) * 0.55f)
                        .border(width = 1.dp, color = signal, shape = CircleShape),
                )
            }
        }

        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(signal),
        )
    }
}

@Composable
private fun rememberPulseProgress(reduced: Boolean): Float {
    if (reduced) return 0f
    val transition = rememberInfiniteTransition(label = "intelPulse")
    val progress by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "pulse",
    )
    return progress
}

/**
 * Streak marker drawn as a bank of signal bars, not a cartoon flame.
 *
 * The tallest bar is always the centre, so the shape reads as a rising signal
 * whether or not it is animating. Milestones get the attention→signal ramp; a
 * normal day stays in one hue.
 */
@Composable
fun StreakIgnition(
    streakCount: Int,
    modifier: Modifier = Modifier,
    isMilestone: Boolean = streakCount > 0 && streakCount % 7 == 0,
) {
    val ext = MaterialTheme.nexus
    val reduced = LocalReducedMotion.current
    val breath = rememberBreath(reduced)

    val attention = ext.attention
    val signal = ext.signal
    val boxSize = if (isMilestone) 48.dp else 32.dp
    val barCount = 5

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(boxSize)) {
            val barWidth = size.width * 0.105f
            val gap = size.width * 0.07f
            val totalWidth = barCount * barWidth + (barCount - 1) * gap
            val startX = (size.width - totalWidth) / 2f
            val baseY = size.height * 0.92f
            val maxBarHeight = size.height * 0.74f

            if (isMilestone) {
                drawCircle(
                    color = attention.copy(alpha = 0.16f),
                    radius = size.minDimension * 0.5f,
                    center = Offset(size.width / 2f, baseY - maxBarHeight * 0.35f),
                )
            }

            for (i in 0 until barCount) {
                val wave = 0.5f + 0.5f * sin(breath * 2f * PI.toFloat() + i * 0.9f)
                val centreBias = 1f - abs(i - (barCount - 1) / 2f) / barCount
                val barHeight = maxBarHeight * (0.34f + 0.66f * wave) * centreBias

                drawRoundRect(
                    brush = Brush.verticalGradient(
                        colors = if (isMilestone) {
                            listOf(signal, attention)
                        } else {
                            listOf(attention, attention.copy(alpha = 0.72f))
                        },
                    ),
                    topLeft = Offset(startX + i * (barWidth + gap), baseY - barHeight),
                    size = Size(barWidth, barHeight),
                    cornerRadius = CornerRadius(barWidth / 2f),
                )
            }

            // Baseline rule: the bars sit on something, so they read as a reading.
            drawLine(
                color = attention.copy(alpha = 0.35f),
                start = Offset(startX - gap, baseY + gap * 0.4f),
                end = Offset(startX + totalWidth + gap, baseY + gap * 0.4f),
                strokeWidth = 1.dp.toPx(),
            )
        }
    }
}

@Composable
private fun rememberBreath(reduced: Boolean): Float {
    if (reduced) return 0.35f
    val transition = rememberInfiniteTransition(label = "streakBreath")
    val value by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(NexusAnimation.Duration.dramatic * 2, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "breath",
    )
    return value
}

/**
 * Reveal-on-scroll: content arrives from below and fades up.
 *
 * Travel is deliberately small (16dp). Anything larger turns a list into a
 * slideshow; this is meant to look like the panel settling, not flying in.
 */
@Composable
fun NexusReveal(
    isVisible: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val reduced = LocalReducedMotion.current
    val travel = with(LocalDensity.current) { 16.dp.toPx() }
    val progress by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = if (reduced) {
            snap()
        } else {
            tween(
                durationMillis = NexusAnimation.Duration.emphasized,
                easing = NexusAnimation.Easing.emphasizedDecelerate,
            )
        },
        label = "reveal",
    )

    Box(
        modifier = modifier.graphicsLayer {
            alpha = progress
            translationY = (1f - progress) * travel
        },
        contentAlignment = Alignment.Center,
    ) {
        content()
    }
}

/**
 * Card that can be pushed aside to act on it.
 *
 * Dragging tracks the finger 1:1, tilts proportionally to the distance travelled,
 * and only fires a callback once the card has actually crossed the threshold —
 * otherwise it settles back. The haptic fires on commit, not on touch-down, so it
 * confirms a decision rather than a tap.
 */
@Composable
fun SwipeableCard(
    modifier: Modifier = Modifier,
    onSwipeLeft: () -> Unit = {},
    onSwipeRight: () -> Unit = {},
    content: @Composable () -> Unit,
) {
    val reduced = LocalReducedMotion.current
    val threshold = with(LocalDensity.current) { 96.dp.toPx() }
    val haptics = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    val offsetX = remember { Animatable(0f) }

    Box(
        modifier = modifier
            .graphicsLayer {
                translationX = offsetX.value
                rotationZ = (offsetX.value / threshold).coerceIn(-1f, 1f) * 4f
            }
            .pointerInput(threshold, reduced) {
                detectDragGestures(
                    onDragEnd = {
                        val settled = offsetX.value
                        scope.launch {
                            when {
                                settled <= -threshold -> {
                                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onSwipeLeft()
                                    offsetX.snapTo(0f)
                                }
                                settled >= threshold -> {
                                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onSwipeRight()
                                    offsetX.snapTo(0f)
                                }
                                else -> offsetX.animateTo(
                                    targetValue = 0f,
                                    animationSpec = if (reduced) {
                                        snap()
                                    } else {
                                        tween(
                                            durationMillis = NexusAnimation.Duration.fast,
                                            easing = NexusAnimation.Easing.emphasizedDecelerate,
                                        )
                                    },
                                )
                            }
                        }
                    },
                    onDragCancel = {
                        scope.launch {
                            offsetX.animateTo(0f, NexusAnimation.Tween.press)
                        }
                    },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        scope.launch { offsetX.snapTo(offsetX.value + dragAmount.x) }
                    },
                )
            },
        contentAlignment = Alignment.Center,
    ) {
        content()
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
        size = Size(radius * 2, radius * 2),
        style = Stroke(width = strokeWidth),
    )
    drawArc(
        color = color,
        startAngle = -90f,
        sweepAngle = sweepAngle,
        useCenter = false,
        topLeft = Offset(center.x - radius, center.y - radius),
        size = Size(radius * 2, radius * 2),
        style = Stroke(width = strokeWidth),
    )
}
