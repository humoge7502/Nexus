package com.nexus.designsystem.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween

object NexusAnimation {
    object Duration {
        val instant = 0
        val micro = 100
        val fast = 200
        val standard = 300
        val emphasized = 500
        val long = 700
        val dramatic = 1000
    }

    object Easing {
        val standard = FastOutSlowInEasing
        val emphasized = CubicBezierEasing(0.2f, 0f, 0f, 1f)
        val emphasizedAccelerate = CubicBezierEasing(0.3f, 0f, 0.8f, 0.15f)
        val emphasizedDecelerate = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1f)
        val enter = FastOutSlowInEasing
        val exit = CubicBezierEasing(0.4f, 0f, 1f, 1f)
    }

    object Springs {
        val button = spring<Float>(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        )
        val card = spring<Float>(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMedium
        )
        val list = spring<Float>(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMediumLow
        )
        val achievementUnlock = spring<Float>(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        )
    }

    object Tween {
        val screenEnter = tween<Float>(
            durationMillis = Duration.standard,
            easing = Easing.emphasizedDecelerate
        )
        val screenExit = tween<Float>(
            durationMillis = Duration.fast,
            easing = Easing.emphasizedAccelerate
        )
        val cardFadeIn = tween<Float>(
            durationMillis = Duration.emphasized,
            easing = Easing.emphasizedDecelerate
        )
        val numberCount = tween<Float>(
            durationMillis = Duration.emphasized,
            easing = Easing.emphasizedDecelerate
        )
    }

    val buttonPress = Springs.button
    val cardSpring = Springs.card
    val listSpring = Springs.list
    val screenEnter = Tween.screenEnter
    val screenExit = Tween.screenExit
}
