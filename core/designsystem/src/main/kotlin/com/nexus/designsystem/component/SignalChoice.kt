package com.nexus.designsystem.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.nexus.designsystem.theme.LocalReducedMotion
import com.nexus.designsystem.theme.NexusAnimation
import com.nexus.designsystem.theme.NexusShapes
import com.nexus.designsystem.theme.NexusSpacing
import com.nexus.designsystem.theme.nexus

/**
 * A selectable tag for a set of options.
 *
 * Not a pill: 6dp corners, a hairline edge and a leading graduation tick that
 * only stands up when the option is on. Selection fills the signal container
 * rather than painting the whole chip in primary, so a dense set of options
 * stays calm and the chosen ones still read at a glance.
 *
 * The layout is 44dp minimum, so it clears the touch-target floor even when the
 * text is short.
 */
@Composable
fun SignalChoice(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val ext = MaterialTheme.nexus
    val haptic = LocalHapticFeedback.current
    val reducedMotion = LocalReducedMotion.current
    val idleContent = MaterialTheme.colorScheme.onSurfaceVariant

    val container by animateColorAsState(
        targetValue = if (selected) ext.signalContainer else Color.Transparent,
        animationSpec = tween(if (reducedMotion) 0 else NexusAnimation.Duration.fast),
        label = "choiceContainer",
    )
    val content by animateColorAsState(
        targetValue = when {
            !enabled -> idleContent.copy(alpha = 0.45f)
            selected -> ext.onSignalContainer
            else -> idleContent
        },
        animationSpec = tween(if (reducedMotion) 0 else NexusAnimation.Duration.fast),
        label = "choiceContent",
    )
    val edge by animateColorAsState(
        targetValue = when {
            selected -> ext.signal.copy(alpha = 0.7f)
            else -> ext.hairlineStrong
        },
        animationSpec = tween(if (reducedMotion) 0 else NexusAnimation.Duration.fast),
        label = "choiceEdge",
    )

    Row(
        modifier = modifier
            .heightIn(min = NexusSpacing.minTouchTarget)
            .clip(NexusShapes.xs)
            .background(container)
            .border(NexusSpacing.hairline, edge, NexusShapes.xs)
            .toggleable(
                value = selected,
                enabled = enabled,
                role = Role.Checkbox,
                onValueChange = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onClick()
                },
            )
            .padding(horizontal = NexusSpacing.sm, vertical = NexusSpacing.xs),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(NexusSpacing.xs),
    ) {
        Canvas(modifier = Modifier.size(width = 6.dp, height = 12.dp)) {
            val lit = selected && enabled
            val markColor = if (lit) ext.signal else content.copy(alpha = 0.45f)
            val markHeight = if (lit) size.height else size.height * 0.5f
            drawLine(
                color = markColor,
                start = Offset(size.width / 2f, size.height - markHeight),
                end = Offset(size.width / 2f, size.height),
                strokeWidth = 2.dp.toPx(),
                cap = StrokeCap.Round,
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = content,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Spacer(modifier = Modifier.width(2.dp))
    }
}
