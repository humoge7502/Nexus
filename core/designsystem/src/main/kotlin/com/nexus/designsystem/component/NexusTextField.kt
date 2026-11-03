package com.nexus.designsystem.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.sp
import com.nexus.designsystem.theme.NexusShapes
import com.nexus.designsystem.theme.nexus
import java.util.Locale

/**
 * A text field that belongs to the instrument.
 *
 * The Material field is kept underneath because that is where the platform's
 * IME, autofill and accessibility behaviour live — only the surface is redrawn:
 * a near-square shape, a mono-tracked field name, a hairline that lights in
 * signal on focus, and a faint inset well rather than a floating white box.
 */
// Mirrors OutlinedTextField's surface so callers keep the platform's IME,
// autofill and accessibility behaviour; bundling these into a config object
// would make every call site worse than the rule it satisfies.
@Composable
@Suppress("LongParameterList")
fun NexusTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    leadingIcon: (@Composable () -> Unit)? = null,
    isError: Boolean = false,
    supportingText: String? = null,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    trailingIcon: (@Composable () -> Unit)? = null,
    singleLine: Boolean = true,
    /** How many lines the field opens with — prose answers deserve room to
     *  begin, not a one-line sliver that grows only after typing. */
    minLines: Int = 1,
    /** Where multiline entry stops; ignored while [singleLine] is true. */
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    enabled: Boolean = true,
) {
    val scheme = MaterialTheme.colorScheme
    val ext = MaterialTheme.nexus

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        enabled = enabled,
        singleLine = singleLine,
        minLines = minLines,
        maxLines = maxLines,
        isError = isError,
        textStyle = MaterialTheme.typography.bodyLarge,
        label = {
            Text(
                text = label.uppercase(Locale.ROOT),
                style = MaterialTheme.typography.labelMedium.copy(letterSpacing = 0.8.sp),
                fontWeight = FontWeight.SemiBold,
            )
        },
        placeholder = if (placeholder != null) {
            {
                Text(
                    text = placeholder,
                    style = MaterialTheme.typography.bodyMedium,
                    color = scheme.onSurfaceVariant.copy(alpha = 0.6f),
                )
            }
        } else {
            null
        },
        supportingText = if (supportingText != null) {
            { Text(text = supportingText, style = MaterialTheme.typography.bodySmall) }
        } else {
            null
        },
        visualTransformation = visualTransformation,
        keyboardOptions = keyboardOptions,
        keyboardActions = keyboardActions,
        leadingIcon = leadingIcon,
        trailingIcon = trailingIcon,
        shape = NexusShapes.sm,
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = scheme.onSurface,
            unfocusedTextColor = scheme.onSurface,
            focusedContainerColor = scheme.surfaceContainerLow.copy(alpha = 0.65f),
            unfocusedContainerColor = scheme.surfaceContainerLow.copy(alpha = 0.35f),
            focusedBorderColor = ext.signal.copy(alpha = 0.85f),
            unfocusedBorderColor = ext.hairlineStrong,
            errorBorderColor = scheme.error,
            cursorColor = ext.signal,
            focusedLabelColor = ext.signal,
            unfocusedLabelColor = scheme.onSurfaceVariant,
            errorLabelColor = scheme.error,
        ),
    )
}
