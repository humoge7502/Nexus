package com.nexus.designsystem.effect

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import com.nexus.designsystem.theme.NexusColors

@Composable
fun BlinkingCursor(modifier: Modifier = Modifier) {
    // An infinite animation, so it gates on reduced motion like every other
    // loop in the design system: statically lit instead of blinking forever.
    if (com.nexus.designsystem.theme.LocalReducedMotion.current) {
        Box(
            modifier = modifier
                .size(width = 2.dp, height = 20.dp)
                .background(NexusColors.NexusBlue),
        )
        return
    }
    val infiniteTransition = rememberInfiniteTransition(label = "blinkingCursor")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 530),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "cursorAlpha",
    )
    Box(
        modifier = modifier
            .size(width = 2.dp, height = 20.dp)
            .alpha(alpha)
            .background(NexusColors.NexusBlue),
    )
}
