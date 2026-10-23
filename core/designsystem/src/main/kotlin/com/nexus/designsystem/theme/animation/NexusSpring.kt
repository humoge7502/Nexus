package com.nexus.designsystem.theme.animation

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring

object NexusSpring {
    val Heavy = spring<Float>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessLow,
    )
    val Medium = spring<Float>(
        dampingRatio = Spring.DampingRatioLowBouncy,
        stiffness = Spring.StiffnessMedium,
    )
    val Light = spring<Float>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessHigh,
    )
    val Playful = spring<Float>(
        dampingRatio = 0.4f,
        stiffness = Spring.StiffnessMediumLow,
    )
    val Snappy = spring<Float>(
        dampingRatio = 0.8f,
        stiffness = Spring.StiffnessHigh,
    )
}
