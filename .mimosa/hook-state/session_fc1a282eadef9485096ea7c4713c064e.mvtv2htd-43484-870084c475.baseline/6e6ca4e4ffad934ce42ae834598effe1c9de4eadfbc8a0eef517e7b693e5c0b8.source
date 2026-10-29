package com.nexus.designsystem.effect

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

/**
 * One-time entrance for above-the-fold blocks: a short fade with a spring lift.
 * Animates in the draw phase only, so nothing re-measures, and Compose collapses
 * it when the system animator scale is off.
 */
@Composable
fun RevealOnLoad(
    modifier: Modifier = Modifier,
    delayMillis: Int = 0,
    lift: Dp = 12.dp,
    content: @Composable () -> Unit,
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        if (delayMillis > 0) delay(delayMillis.toLong())
        visible = true
    }

    val alpha by animateFloatAsState(
        targetValue = if (visible) 1f else 0f,
        animationSpec = tween(360, easing = FastOutSlowInEasing),
        label = "revealAlpha",
    )
    val liftPx = with(LocalDensity.current) { lift.toPx() }
    val offset by animateFloatAsState(
        targetValue = if (visible) 0f else liftPx,
        animationSpec = spring(dampingRatio = 0.9f, stiffness = Spring.StiffnessLow),
        label = "revealLift",
    )

    Box(
        modifier = modifier.graphicsLayer {
            this.alpha = alpha
            translationY = offset
        }
    ) {
        content()
    }
}
