package com.nexus.designsystem.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.nexus.designsystem.R
import com.nexus.designsystem.theme.NexusTypography

/**
 * Instrument header for secondary screens: the back key, a large title with an
 * optional mono meta readout, and a hairline that closes the header off from
 * content. Replaces Material's stock TopAppBar so every pushed screen speaks
 * the same language as the five top-level tabs.
 */
@Composable
fun NexusTopBar(
    title: String,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
    subtitle: String? = null,
    meta: String? = null,
    showDivider: Boolean = true,
    actions: @Composable RowScope.() -> Unit = {},
) {
    NexusTopBarLayout(
        modifier = modifier,
        onBack = onBack,
        meta = meta,
        showDivider = showDivider,
        actions = actions,
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onBackground,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        if (subtitle != null) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
fun NexusTopBar(
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
    meta: String? = null,
    showDivider: Boolean = true,
    title: @Composable () -> Unit,
    actions: @Composable RowScope.() -> Unit = {},
) {
    NexusTopBarLayout(
        modifier = modifier,
        onBack = onBack,
        meta = meta,
        showDivider = showDivider,
        title = title,
        actions = actions,
    )
}

@Composable
private fun NexusTopBarLayout(
    modifier: Modifier,
    onBack: (() -> Unit)?,
    meta: String?,
    showDivider: Boolean,
    actions: @Composable RowScope.() -> Unit,
    title: @Composable () -> Unit,
) {
    val haptic = LocalHapticFeedback.current
    Column(modifier = modifier.fillMaxWidth().statusBarsPadding()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = if (onBack != null) 12.dp else 24.dp, end = 8.dp, top = 6.dp, bottom = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (onBack != null) {
                IconButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onBack()
                    },
                    modifier = Modifier.padding(end = 2.dp).size(36.dp),
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.cd_back),
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.onBackground,
                    )
                }
            }
            Column(modifier = Modifier.weight(1f)) { title() }
            if (meta != null) {
                Text(
                    text = meta,
                    style = NexusTypography.TelemetryMicro,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 8.dp, end = 4.dp),
                )
            }
            actions()
        }
        if (showDivider) {
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 24.dp),
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f),
            )
        }
    }
}
