package com.nexus.designsystem.component

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import com.nexus.designsystem.theme.NexusShapes

@Composable
fun NexusCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    gradient: List<Color>? = null,
    glassEffect: Boolean = false,
    shape: Shape = NexusShapes.md,
    content: @Composable BoxScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val haptic = LocalHapticFeedback.current

    val scale by animateFloatAsState(
        targetValue = if (isPressed && onClick != null) 0.97f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "cardScale"
    )

    val elevation by animateDpAsState(
        targetValue = if (isPressed && onClick != null) 2.dp else 6.dp,
        label = "cardElevation"
    )

    val cardModifier = modifier
        .scale(scale)
        .let { mod ->
            if (onClick != null) {
                mod.clickable(
                    interactionSource = interactionSource,
                    indication = null
                ) {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onClick()
                }
            } else mod
        }

    Surface(
        modifier = cardModifier,
        shape = shape,
        color = when {
            gradient != null -> Color.Transparent
            glassEffect -> MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
            else -> MaterialTheme.colorScheme.surface
        },
        tonalElevation = if (glassEffect) 0.dp else elevation,
        shadowElevation = if (glassEffect) 0.dp else elevation,
    ) {
        Box(
            modifier = Modifier
                .then(
                    if (gradient != null) Modifier.background(
                        brush = Brush.linearGradient(gradient), shape = shape
                    ) else Modifier
                )
                .then(
                    if (glassEffect) Modifier
                        .background(
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                            shape = shape
                        )
                        .border(
                            width = 0.5.dp,
                            brush = Brush.verticalGradient(
                                listOf(Color.White.copy(alpha = 0.3f), Color.White.copy(alpha = 0.05f))
                            ),
                            shape = shape
                        )
                    else Modifier
                )
                .clip(shape)
                .padding(16.dp),
            contentAlignment = Alignment.TopStart
        ) {
            content()
        }
    }
}
