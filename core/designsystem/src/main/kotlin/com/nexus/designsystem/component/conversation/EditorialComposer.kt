package com.nexus.designsystem.component.conversation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.nexus.designsystem.component.NexusTextField
import com.nexus.designsystem.component.pressScale
import com.nexus.designsystem.theme.NexusGradients
import com.nexus.designsystem.theme.NexusShapes
import com.nexus.designsystem.theme.NexusSpacing
import com.nexus.designsystem.theme.NexusTypography
import com.nexus.designsystem.theme.nexus

/**
 * The input end of a conversation.
 *
 * Lifted out of the AI feature so every screen that takes a sentence from the
 * user — chat, research, goal editing — drives the same plate, the same send
 * affordance and the same send rule: enabled only when there is something to
 * send, never a button that lies about being pressable.
 */
@Composable
fun ConversationComposer(
    value: String,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit,
    modifier: Modifier = Modifier,
    label: String = "MESSAGE",
    placeholder: String = "",
    enabled: Boolean = true,
    supporting: String? = null,
) {
    val ext = MaterialTheme.nexus
    val canSend = enabled && value.isNotBlank()

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = NexusSpacing.gutter),
            verticalAlignment = Alignment.Bottom,
        ) {
            NexusTextField(
                value = value,
                onValueChange = onValueChange,
                label = label,
                placeholder = placeholder,
                modifier = Modifier.weight(1f),
                enabled = enabled,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = { if (canSend) onSend() }),
            )
            Spacer(Modifier.width(NexusSpacing.xs))
            SendPlate(canSend = canSend, onSend = onSend)
        }
        if (!supporting.isNullOrBlank()) {
            Text(
                text = supporting,
                style = NexusTypography.TelemetryMicro,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f),
                modifier = Modifier.padding(
                    start = NexusSpacing.gutter,
                    end = NexusSpacing.gutter,
                    top = NexusSpacing.xs,
                ),
            )
        }
        // The rail lives under the plate rather than above it: an opener is a
        // suggestion, and suggestions should not sit on the path to the keyboard.
        Spacer(Modifier.height(NexusSpacing.xxs))
        Box(
            modifier = Modifier
                .padding(horizontal = NexusSpacing.gutter)
                .fillMaxWidth()
                .height(NexusSpacing.hairline)
                .background(ext.hairline),
        )
    }
}

/** The send control. Gradient-filled only when it will actually fire. */
@Composable
private fun SendPlate(
    canSend: Boolean,
    onSend: () -> Unit,
    sendDescription: String = "Send",
) {
    val ext = MaterialTheme.nexus
    val scheme = MaterialTheme.colorScheme
    val haptic = LocalHapticFeedback.current
    val shape = NexusShapes.sm

    Box(
        modifier = Modifier
            .size(52.dp)
            .then(if (canSend) Modifier.pressScale() else Modifier)
            .clip(shape)
            .then(
                if (canSend) {
                    Modifier.background(Brush.linearGradient(NexusGradients.SignalSweep))
                } else {
                    Modifier
                        .background(scheme.surfaceContainerLow)
                        .border(NexusSpacing.hairline, ext.hairlineStrong, shape)
                },
            )
            .clickable(enabled = canSend, role = Role.Button) {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onSend()
            },
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.Send,
            contentDescription = sendDescription,
            tint = if (canSend) ext.ink else scheme.onSurfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.size(20.dp),
        )
    }
}
