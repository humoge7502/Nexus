package com.nexus.designsystem.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.nexus.designsystem.theme.LocalReducedMotion
import com.nexus.designsystem.theme.NexusAnimation
import com.nexus.designsystem.theme.motionMillis
import com.nexus.designsystem.theme.nexus
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin

/**
 * The achievement-unlocked celebration: a radial particle burst behind a
 * centered badge card naming the achievement.
 *
 * One-shot and self-dismissing (tap to skip). The plan's motion rule —
 * "achievement unlocked: particle burst" — is the one place flourish is
 * allowed; everywhere else stays restrained. With reduced motion the
 * particles are skipped entirely and only the card appears.
 *
 * @param badgeName the achievement that just unlocked, shown on the card.
 * @param onDismiss called when the celebration finishes or is tapped away.
 */
@Composable
fun AchievementBurst(
    badgeName: String,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val extended = MaterialTheme.nexus
    val reducedMotion = LocalReducedMotion.current
    val burstDuration = motionMillis(NexusAnimation.Duration.dramatic)
    val progress = remember { Animatable(0f) }
    LaunchedEffect(badgeName) {
        progress.animateTo(
            1f,
            animationSpec = tween(
                durationMillis = burstDuration,
                easing = NexusAnimation.Easing.emphasizedDecelerate,
            ),
        )
        delay(2600)
        onDismiss()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.55f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
            ) { onDismiss() },
        contentAlignment = Alignment.Center,
    ) {
        if (!reducedMotion) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val p = progress.value
                if (p <= 0f || p >= 1f) return@Canvas
                val center = Offset(size.width / 2f, size.height / 2f)
                val maxDist = size.minDimension * 0.42f
                val particles = 28
                repeat(particles) { i ->
                    val angle = (2.0 * Math.PI * i / particles) + (if (i % 2 == 0) 0.18 else -0.12)
                    val dist = maxDist * (0.55f + 0.45f * p) * (if (i % 3 == 0) 1f else 0.78f)
                    val pos = Offset(
                        center.x + (dist * cos(angle)).toFloat(),
                        center.y + (dist * sin(angle)).toFloat(),
                    )
                    val color = when (i % 3) {
                        0 -> extended.signal
                        1 -> extended.attention
                        else -> Color.White
                    }
                    drawCircle(
                        color = color.copy(alpha = (1f - p)),
                        radius = (if (i % 2 == 0) 5f else 3f) * (1f - p * 0.5f),
                        center = pos,
                    )
                }
                // Expanding halo ring.
                drawCircle(
                    color = extended.signal.copy(alpha = 0.5f * (1f - p)),
                    radius = maxDist * 0.35f * p + 24f,
                    center = center,
                    style = Stroke(width = 2f),
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 48.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "★",
                style = MaterialTheme.typography.headlineMedium,
                color = extended.signal,
            )
            Text(
                text = "Achievement unlocked",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 6.dp, bottom = 4.dp),
            )
            Text(
                text = badgeName.replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
            Text(
                text = "Tap to continue",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 14.dp),
            )
        }
    }
}
