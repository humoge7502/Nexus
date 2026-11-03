package com.nexus.designsystem.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.nexus.designsystem.theme.NexusShapes
import com.nexus.designsystem.theme.NexusTypography
import com.nexus.designsystem.theme.nexus

/** One jumpable target: a destination, or a search hand-off. The palette is
 *  business-logic-free — the app shell decides what is in the list. */
data class NexusCommand(
    val id: String,
    val title: String,
    val subtitle: String? = null,
    val category: String = "",
)

/**
 * The command palette: every place the product can take you, behind one
 * query. An editorial index rather than a menu — mono category, sans title,
 * hairline rows — because the fastest way through a dense product is a
 * table of contents you can type into.
 *
 * Copy arrives as parameters (the design system carries no strings); the
 * caller owns the command list and what selection does.
 */
@Composable
fun NexusCommandPalette(
    query: String,
    onQueryChange: (String) -> Unit,
    hint: String,
    emptyLabel: String,
    commands: List<NexusCommand>,
    onSelect: (NexusCommand) -> Unit,
    onDismiss: () -> Unit,
) {
    val focusRequester = remember { FocusRequester() }
    val trimmed = query.trim()

    val matches = if (trimmed.isEmpty()) {
        commands
    } else {
        commands.filter {
            it.title.contains(trimmed, ignoreCase = true) ||
                it.subtitle?.contains(trimmed, ignoreCase = true) == true ||
                it.category.contains(trimmed, ignoreCase = true)
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            modifier = Modifier
                .padding(horizontal = 12.dp)
                .fillMaxWidth()
                .widthIn(max = 560.dp)
                .navigationBarsPadding()
                .imePadding(),
            shape = NexusShapes.xl,
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            tonalElevation = 3.dp,
        ) {
            Column(Modifier.padding(top = 6.dp, bottom = 10.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 18.dp, end = 6.dp)
                        .height(52.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = MaterialTheme.nexus.signal,
                    )
                    Spacer(Modifier.width(12.dp))
                    BasicTextField(
                        value = query,
                        onValueChange = onQueryChange,
                        modifier = Modifier
                            .weight(1f)
                            .focusRequester(focusRequester),
                        singleLine = true,
                        textStyle = MaterialTheme.typography.bodyLarge.copy(
                            color = MaterialTheme.colorScheme.onSurface,
                        ),
                        cursorBrush = SolidColor(MaterialTheme.nexus.signal),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(
                            onSearch = { matches.firstOrNull()?.let(onSelect) },
                        ),
                        decorationBox = { inner ->
                            if (query.isEmpty()) {
                                Text(
                                    text = hint,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            inner()
                        },
                    )
                    if (query.isNotEmpty()) {
                        NexusIconButton(
                            icon = Icons.Default.Close,
                            contentDescription = "Clear query",
                            onClick = { onQueryChange("") },
                        )
                    }
                }
                HorizontalDivider(thickness = 1.dp, color = MaterialTheme.nexus.hairline)

                if (matches.isEmpty()) {
                    Text(
                        text = emptyLabel,
                        style = NexusTypography.Voice,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 28.dp),
                    )
                } else {
                    PaletteResults(matches = matches, onSelect = onSelect)
                }
            }
        }
    }

    LaunchedEffect(Unit) { focusRequester.requestFocus() }
}

/** The index itself: one hairline row per place, category in mono at the
 *  left like a contents page. */
@Composable
private fun PaletteResults(
    matches: List<NexusCommand>,
    onSelect: (NexusCommand) -> Unit,
) {
    LazyColumn {
        items(matches, key = { it.id }) { command ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSelect(command) }
                    // A row this flat still owes the finger a full target,
                    // not a token one.
                    .padding(horizontal = 18.dp, vertical = 13.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (command.category.isNotEmpty()) {
                    Text(
                        text = command.category,
                        style = NexusTypography.DataLabel,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.width(84.dp),
                    )
                    Spacer(Modifier.width(12.dp))
                }
                Column(Modifier.weight(1f)) {
                    Text(
                        text = command.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    if (command.subtitle != null) {
                        Text(
                            text = command.subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            HorizontalDivider(
                thickness = 1.dp,
                color = MaterialTheme.nexus.hairline.copy(alpha = 0.6f),
            )
        }
    }
}
