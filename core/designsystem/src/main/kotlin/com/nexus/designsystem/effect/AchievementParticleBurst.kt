package com.nexus.designsystem.effect

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import kotlin.random.Random

private data class Particle(
    var x: Float, var y: Float,
    var vx: Float, var vy: Float,
    var alpha: Float = 1f,
    val radius: Float,
    val color: Color,
    var life: Float = 1f,
)

@Composable
fun AchievementParticleBurst(
    trigger: Boolean,
    modifier: Modifier = Modifier,
    particleCount: Int = 60,
) {
    val particles = remember { mutableStateListOf<Particle>() }
    val colors = remember {
        listOf(
            Color(0xFF4F8EF7), Color(0xFF00D9A3), Color(0xFFF72585),
            Color(0xFF7B5EA7), Color(0xFFFF6B35), Color(0xFFE8EDFB),
        )
    }
    LaunchedEffect(trigger) {
        if (!trigger) return@LaunchedEffect
        particles.clear()
        repeat(particleCount) {
            val angle = Random.nextFloat() * Math.PI.toFloat() * 2
            val speed = Random.nextFloat() * 0.02f + 0.005f
            particles.add(Particle(
                x = 0.5f, y = 0.5f,
                vx = kotlin.math.cos(angle) * speed,
                vy = kotlin.math.sin(angle) * speed,
                radius = Random.nextFloat() * 8f + 3f,
                color = colors.random(),
            ))
        }
        var lastFrameNanos = 0L
        while (particles.any { it.life > 0 }) {
            withFrameNanos { frameNanos ->
                val delta = if (lastFrameNanos == 0L) 16_666_666L else frameNanos - lastFrameNanos
                lastFrameNanos = frameNanos
                val deltaFraction = delta / 16_666_666f
                particles.forEach { p ->
                    p.x += p.vx * deltaFraction
                    p.y += p.vy * deltaFraction
                    p.vy += 0.0003f * deltaFraction
                    p.life -= 0.02f * deltaFraction
                    p.alpha = p.life.coerceIn(0f, 1f)
                }
                particles.removeAll { it.life <= 0 }
            }
        }
    }
    Canvas(modifier = modifier) {
        particles.forEach { p ->
            drawCircle(
                color = p.color.copy(alpha = p.alpha),
                radius = p.radius,
                center = Offset(p.x * size.width, p.y * size.height),
            )
        }
    }
}
