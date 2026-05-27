package com.nexus.designsystem.effect

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import kotlin.random.Random

data class Particle(
    val startX: Float,
    val startY: Float,
    val radius: Float,
    val speedX: Float,
    val speedY: Float,
    val alpha: Float,
)

@Composable
fun ParticleBackground(
    particleCount: Int = 30,
    modifier: Modifier = Modifier,
    color: Color = Color(0xFF6366F1),
) {
    val particles = remember {
        List(particleCount) {
            Particle(
                startX = Random.nextFloat(),
                startY = Random.nextFloat(),
                radius = Random.nextFloat() * 3f + 1f,
                speedX = (Random.nextFloat() - 0.5f) * 0.003f,
                speedY = (Random.nextFloat() - 0.5f) * 0.003f,
                alpha = Random.nextFloat() * 0.3f + 0.1f
            )
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "particles")
    val time by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(15000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "time"
    )

    Canvas(modifier = modifier) {
        particles.forEach { particle ->
            val rawX = (particle.startX + particle.speedX * time * 2000)
            val rawY = (particle.startY + particle.speedY * time * 2000)
            val x = (((rawX % 1f) + 1f) % 1f) * size.width
            val y = (((rawY % 1f) + 1f) % 1f) * size.height

            drawCircle(
                color = color.copy(alpha = particle.alpha),
                radius = particle.radius * size.minDimension / 200f,
                center = Offset(x, y)
            )
        }
    }
}
