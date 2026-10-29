package com.nexus.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.nexus.designsystem.theme.NexusShapes

/**
 * The one card primitive. Separates itself from the ground with tone and a
 * hairline rather than stacked shadows, and springs under the finger when it
 * is tappable. Gradient cards are signature surfaces: use them sparingly.
 */
@Composable
fun NexusCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    gradient: List<Color>? = null,
    glassEffect: Boolean = false,
    containerColor: Color? = null,
    shape: Shape = NexusShapes.md,
    content: @Composable ColumnScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val haptic = LocalHapticFeedback.current

    val resolvedColor = when {
        gradient != null -> Color.Transparent
        containerColor != null -> containerColor
        glassEffect -> MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.72f)
        else -> MaterialTheme.colorScheme.surfaceContainerLow
    }

    val cardModifier = modifier
        .let { mod ->
            if (onClick != null) {
                mod
                    .pressScale()
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        role = Role.Button,
                    ) {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onClick()
                    }
            } else mod
        }

    Surface(
        modifier = cardModifier,
        shape = shape,
        color = resolvedColor,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        border = if (gradient == null) {
            BorderStroke(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.9f),
            )
        } else {
            null
        },
    ) {
        Box(
            modifier = Modifier
                .then(
                    if (gradient != null) Modifier.background(
                        brush = Brush.linearGradient(gradient), shape = shape
                    ) else Modifier
                )
                .clip(shape)
                .padding(16.dp),
            contentAlignment = Alignment.TopStart
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                content()
            }
        }
    }
}
