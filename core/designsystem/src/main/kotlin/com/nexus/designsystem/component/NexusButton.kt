package com.nexus.designsystem.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nexus.designsystem.theme.LocalReducedMotion
import com.nexus.designsystem.theme.NexusAnimation
import com.nexus.designsystem.theme.NexusShapes
import com.nexus.designsystem.theme.motionMillis
import com.nexus.designsystem.theme.onAccent
import com.nexus.designsystem.theme.nexus

/**
 * Where a committed action stands. Derived from [NexusButton] inputs; the caller
 * owns the truth (a real operation's lifecycle), the button only speaks it.
 */
private enum class CommitPhase { Idle, Running, Settled }

/**
 * The primary action. A gradient button paints the gradient on its own surface
 * — no wrapper box, no double-click target — and takes its content colour from
 * the lightest stop so an acid-lime button gets ink text rather than white.
 *
 * A commit is spoken in three phases: [icon]/[text] until pressed, the spinner
 * while `loading`, then — only when the caller reports [settled] with the real
 * result in hand — the check. Settled is never simulated: it must be driven by
 * the actual operation's outcome, never by a timer. The phase swap is a
 * crossfade where the old state leaves faster than the new one arrives, so a
 * save reads as an event, not a decoration.
 */
@Composable
fun NexusButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    gradient: List<Color>? = null,
    shape: Shape = NexusShapes.md,
    enabled: Boolean = true,
    loading: Boolean = false,
    /** True once the real operation behind this button has succeeded. */
    settled: Boolean = false,
    /** The label the settled phase speaks; defaults to [text] with the check. */
    settledText: String? = null,
    /** A primary action usually spans its row; pass false for an inline CTA
     *  that should hug its label instead of stretching. */
    expand: Boolean = true,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val haptic = LocalHapticFeedback.current
    val reducedMotion = LocalReducedMotion.current

    val phase = when {
        loading -> CommitPhase.Running
        settled -> CommitPhase.Settled
        else -> CommitPhase.Idle
    }
    // Captured in composition so the phase swap collapses to zero under
    // reduced motion — transitionSpec itself is not composable context.
    val enterMillis = motionMillis(NexusAnimation.Duration.standard)
    val exitMillis = motionMillis(NexusAnimation.Duration.micro)
    // The settle lands as a felt confirmation, not only a drawn one.
    LaunchedEffect(phase) {
        if (phase == CommitPhase.Settled) {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        }
    }

    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled && !loading) 0.97f else 1f,
        animationSpec = tween(
            durationMillis = if (reducedMotion) 0 else NexusAnimation.Duration.micro,
            easing = NexusAnimation.Easing.standard,
        ),
        label = "buttonScale"
    )

    val gradientContentColor = gradient?.let {
        onAccent(it.maxByOrNull { stop -> stop.luminance() } ?: it.last())
    }

    Button(
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            onClick()
        },
        modifier = modifier
            .then(if (expand) Modifier.fillMaxWidth() else Modifier)
            .heightIn(min = 52.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .then(
                if (gradient != null) {
                    Modifier
                        .clip(shape)
                        .background(Brush.linearGradient(gradient), shape)
                        .alpha(if (enabled) 1f else 0.45f)
                } else {
                    Modifier
                }
            ),
        enabled = enabled && !loading,
        shape = shape,
        interactionSource = interactionSource,
        colors = if (gradientContentColor == null) {
            ButtonDefaults.buttonColors()
        } else {
            ButtonDefaults.buttonColors(
                containerColor = Color.Transparent,
                contentColor = gradientContentColor,
                disabledContainerColor = Color.Transparent,
                disabledContentColor = gradientContentColor.copy(alpha = 0.6f),
            )
        }
    ) {
        AnimatedContent(
            targetState = phase,
            transitionSpec = {
                // Exits quicker than entrances: the old state yields, the new
                // one lands.
                fadeIn(tween(enterMillis, easing = NexusAnimation.Easing.emphasizedDecelerate)) togetherWith
                    fadeOut(tween(exitMillis, easing = NexusAnimation.Easing.exit))
            },
            label = "buttonCommit",
        ) { target ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                when (target) {
                    CommitPhase.Running -> {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = LocalContentColor.current,
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = text,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                    CommitPhase.Settled -> {
                        // The glyph is decorative; the settled label carries the
                        // meaning for TalkBack, as everywhere else.
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.padding(end = 8.dp),
                        )
                        Text(
                            text = settledText ?: text,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                    CommitPhase.Idle -> {
                        if (icon != null) {
                            Icon(
                                imageVector = icon,
                                contentDescription = null,
                                modifier = Modifier.padding(end = 8.dp),
                            )
                        }
                        Text(
                            text = text,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }
        }
    }
}

/**
 * The secondary action: same geometry and press response as [NexusButton], but
 * hollow. The border is a hairline rather than a full outline, so it sits behind
 * the primary action in the hierarchy without looking disabled.
 */
@Composable
fun NexusOutlineButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true,
    /** Matches [NexusButton.expand]: false for an inline secondary action. */
    expand: Boolean = true,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val haptic = LocalHapticFeedback.current
    val reducedMotion = LocalReducedMotion.current
    val edge = MaterialTheme.nexus.hairlineStrong

    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) 0.97f else 1f,
        animationSpec = tween(
            durationMillis = if (reducedMotion) 0 else NexusAnimation.Duration.micro,
            easing = NexusAnimation.Easing.standard,
        ),
        label = "outlineButtonScale",
    )

    OutlinedButton(
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            onClick()
        },
        modifier = modifier
            .then(if (expand) Modifier.fillMaxWidth() else Modifier)
            .heightIn(min = 52.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            },
        enabled = enabled,
        shape = NexusShapes.md,
        border = BorderStroke(
            width = 1.dp,
            color = if (enabled) edge else edge.copy(alpha = 0.4f),
        ),
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = MaterialTheme.colorScheme.onSurface,
            disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
        ),
        interactionSource = interactionSource,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.padding(end = 8.dp)
                )
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
