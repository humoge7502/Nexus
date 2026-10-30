package com.nexus.designsystem.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf

/**
 * Motion rules for an instrument, not a showreel.
 *
 * UI motion resolves in under 300ms and only ever moves transform or opacity.
 * Entrances use a strong ease-out (0.23, 1, 0.32, 1) so elements arrive fast and
 * settle; exits accelerate away. Springs are near-critically damped — bounce is
 * reserved for a genuine achievement, never for a card appearing.
 *
 * When the system animator scale is 0 (accessibility "remove animations"), the
 * theme provides [LocalReducedMotion] = true and every duration below is read
 * through [motionMillis] so movement collapses to an instant state change.
 */
val LocalReducedMotion = staticCompositionLocalOf { false }

/** Collapses a duration to zero when the user has asked for no motion. */
@Composable
@ReadOnlyComposable
fun motionMillis(millis: Int): Int = if (LocalReducedMotion.current) 0 else millis

object NexusAnimation {
    object Duration {
        val instant = 0
        /** Press feedback. Must land before the finger lifts. */
        val micro = 90
        val fast = 140
        val standard = 220
        /** Longest UI transition allowed outside a hero gauge sweep. */
        val emphasized = 280
        /** Hero-only: the first sweep of a primary instrument. */
        val long = 480
        val dramatic = 700
    }

    object Easing {
        val standard = CubicBezierEasing(0.2f, 0f, 0f, 1f)
        /** Strong ease-out: arrives quickly, settles without overshoot. */
        val emphasized = CubicBezierEasing(0.23f, 1f, 0.32f, 1f)
        val emphasizedAccelerate = CubicBezierEasing(0.3f, 0f, 0.8f, 0.15f)
        val emphasizedDecelerate = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1f)
        val enter = CubicBezierEasing(0.23f, 1f, 0.32f, 1f)
        val exit = CubicBezierEasing(0.4f, 0f, 1f, 1f)
    }

    object Springs {
        val button = spring<Float>(
            dampingRatio = 0.82f,
            stiffness = Spring.StiffnessHigh
        )
        val card = spring<Float>(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMediumLow
        )
        val list = spring<Float>(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMediumLow
        )
        /** The one place bounce is allowed: an unlocked achievement. */
        val achievementUnlock = spring<Float>(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        )
    }

    object Tween {
        val press = tween<Float>(durationMillis = Duration.micro, easing = Easing.standard)
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
