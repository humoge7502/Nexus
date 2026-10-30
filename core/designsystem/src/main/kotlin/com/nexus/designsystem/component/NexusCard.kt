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
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.nexus.designsystem.theme.NexusShapes
import com.nexus.designsystem.theme.NexusSpacing
import com.nexus.designsystem.theme.nexus
import com.nexus.designsystem.theme.onAccent

/**
 * The one card primitive. It separates itself from the ground with tone, a
 * hairline, and a 1dp edge-light that follows the shape — never with stacked
 * shadows. Tappable cards acknowledge the finger in 90ms.
 *
 * Gradient cards are signature surfaces: when one is used, the content colour
 * is derived from the lightest gradient stop so lime surfaces get ink text
 * automatically instead of unreadable white.
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
        .innerLight(shape)

    Surface(
        modifier = cardModifier,
        shape = shape,
        color = resolvedColor,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        border = if (gradient == null) {
            BorderStroke(width = NexusSpacing.hairline, color = MaterialTheme.nexus.hairline)
        } else {
            null
        },
    ) {
        CompositionLocalProvider(
            LocalContentColor provides if (gradient != null) {
                onAccent(gradient.maxByOrNull { it.luminance() } ?: gradient.last())
            } else {
                LocalContentColor.current
            }
        ) {
            Box(
                modifier = Modifier
                    .then(
                        if (gradient != null) {
                            Modifier.background(
                                brush = Brush.linearGradient(gradient),
                                shape = shape,
                            )
                        } else {
                            Modifier
                        }
                    )
                    .clip(shape)
                    .padding(NexusSpacing.gutter),
                contentAlignment = Alignment.TopStart
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    content()
                }
            }
        }
    }
}
