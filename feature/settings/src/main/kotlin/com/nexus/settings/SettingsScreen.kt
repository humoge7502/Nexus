package com.nexus.settings

import androidx.compose.ui.res.stringResource
import com.nexus.settings.R

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.IosShare
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material.icons.filled.BrightnessMedium
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.PlayCircleOutline
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nexus.designsystem.component.NexusIconButton
import com.nexus.designsystem.component.NexusTextField
import com.nexus.designsystem.component.NexusTopBar
import com.nexus.designsystem.theme.NexusColors
import com.nexus.settings.deletion.AccountDeletionScreen

private data class AccentOption(val name: String, val color: Long?)

private val accentOptions = listOf(
    AccentOption("DEFAULT", null),
    AccentOption("AZURE", 0xFF4F8EF7),
    AccentOption("CYAN", 0xFF06B6D4),
    AccentOption("EMERALD", 0xFF10B981),
    AccentOption("AMBER", 0xFFF59E0B),
    AccentOption("ROSE", 0xFFF43F5E),
)

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onGoPro: () -> Unit = {},
    /**
     * What happens when the account and device data are gone. Default clears
     * the task stack and relaunches the app — the boot flow then lands on the
     * auth screen because the sign-in preference was wiped with everything
     * else. The parameter exists so tests can observe the event instead.
     */
    onAccountDeleted: (() -> Unit)? = null,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isDarkMode = uiState.themeMode == "DARK"
    val isAmoled = uiState.amoledEnabled && isDarkMode
    val context = LocalContext.current
    val restartAfterDeletion = remember(context) {
        {
            context.packageManager.getLaunchIntentForPackage(context.packageName)
                ?.apply {
                    addFlags(
                        android.content.Intent.FLAG_ACTIVITY_NEW_TASK or
                            android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK,
                    )
                }
                ?.let(context::startActivity)
            Unit
        }
    }
    val versionName = remember(context) {
        context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: ""
    }
    var showWalkthrough by remember { mutableStateOf(false) }
    var showDeleteAccount by remember { mutableStateOf(false) }
    var showShareHelp by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val exportInFlight by viewModel.exportInFlight.collectAsStateWithLifecycle()

    SettingsExportSnackbarHost(viewModel = viewModel, hostState = snackbarHostState)

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json"),
    ) { uri ->
        if (uri != null) viewModel.exportData(uri)
    }

    if (showDeleteAccount) {
        AccountDeletionScreen(
            onNavigateBack = { showDeleteAccount = false },
            // Caller-supplied hook first; the launcher-intent restart otherwise.
            onDeletionComplete = onAccountDeleted ?: restartAfterDeletion,
        )
        return
    }

    SettingsShell(versionName = versionName, onBack = onBack, hostState = snackbarHostState) {
        SettingsProBanner(onClick = onGoPro)
        SettingsAppearanceSection(
            uiState = uiState,
            isDarkMode = isDarkMode,
            isAmoled = isAmoled,
            onDarkModeChange = viewModel::setDarkMode,
            onAmoledChange = viewModel::setAmoled,
            onAccentColorChange = viewModel::setAccentColor,
        )
        SettingsNotificationsSection(
            enabled = uiState.notificationsEnabled,
            onEnabledChange = viewModel::setNotificationsEnabled,
        )
        SettingsConnectionsSection(
            uiState = uiState,
            onSaveGithub = viewModel::saveGithubUsername,
            onSaveLeetcode = viewModel::saveLeetcodeUsername,
            onSaveCodeforces = viewModel::saveCodeforcesUsername,
            onSaveLinkedin = viewModel::saveLinkedinUrl,
        )
        SettingsDataRightsSection(
            exportInFlight = exportInFlight,
            onPrivacyClick = {
                context.startActivity(
                    android.content.Intent(
                        android.content.Intent.ACTION_VIEW,
                        android.net.Uri.parse(com.nexus.common.Constants.PRIVACY_POLICY_URL),
                    ),
                )
            },
            onShareHelpClick = { showShareHelp = true },
            onExportClick = { if (!exportInFlight) exportLauncher.launch("sagitta-export.json") },
            onDeleteClick = { showDeleteAccount = true },
        )
        SettingsHelpSection(
            versionName = versionName,
            onWalkthroughClick = { showWalkthrough = true },
            onReplayTourClick = {
                // Back first: the walkthrough is drawn by the shell and only
                // appears once a top-level section is on screen.
                onBack()
                viewModel.reopenTour()
            },
        )
    }

    if (showWalkthrough) {
        WalkthroughFilmDialog(onDismiss = { showWalkthrough = false })
    }
    if (showShareHelp) {
        SettingsShareHelpDialog(onDismiss = { showShareHelp = false })
    }
}

/** Turns each export outcome into one snackbar; kept out of the screen body. */
@Composable
private fun SettingsExportSnackbarHost(viewModel: SettingsViewModel, hostState: SnackbarHostState) {
    val exportFailedText = stringResource(R.string.settings_export_failed)
    val exportDoneText = stringResource(R.string.settings_export_done)
    LaunchedEffect(Unit) {
        viewModel.exportEvents.collect { event ->
            hostState.showSnackbar(
                when (event) {
                    ExportEvent.Exported -> exportDoneText
                    ExportEvent.Failed -> exportFailedText
                },
            )
        }
    }
}

/** Top bar, scrollable content slot, and snackbar; sections live in the slot. */
@Composable
private fun SettingsShell(
    versionName: String,
    onBack: () -> Unit,
    hostState: SnackbarHostState,
    content: @Composable ColumnScope.() -> Unit,
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            NexusTopBar(
                title = stringResource(R.string.settings_title),
                onBack = onBack,
                meta = if (versionName.isNotBlank()) "v$versionName" else null,
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp),
                content = content,
            )
        }
        SnackbarHost(
            hostState = hostState,
            modifier = Modifier
                .align(androidx.compose.ui.Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 16.dp),
        )
    }
}

@Composable
private fun SettingsProBanner(onClick: () -> Unit) {
    SettingsItem(
        icon = androidx.compose.material.icons.Icons.Default.WorkspacePremium,
        title = stringResource(R.string.settings_pro_title),
        subtitle = stringResource(R.string.settings_pro_subtitle),
        trailing = {
            Text(
                stringResource(R.string.settings_upgrade),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold,
            )
        },
        onClick = onClick,
    )
}

@Composable
private fun SettingsAppearanceSection(
    uiState: SettingsUiState,
    isDarkMode: Boolean,
    isAmoled: Boolean,
    onDarkModeChange: (Boolean) -> Unit,
    onAmoledChange: (Boolean) -> Unit,
    onAccentColorChange: (String) -> Unit,
) {
    Spacer(modifier = Modifier.height(24.dp))
    SettingsSectionHeader(title = stringResource(R.string.text_appearance))
    SettingsItem(
        icon = Icons.Default.BrightnessMedium,
        title = stringResource(R.string.settings_dark_mode),
        subtitle = stringResource(R.string.settings_dark_mode_subtitle),
        trailing = {
            Switch(checked = isDarkMode, onCheckedChange = onDarkModeChange)
        },
    )
    SettingsItem(
        icon = Icons.Default.DarkMode,
        title = stringResource(R.string.settings_amoled_title),
        subtitle = if (isDarkMode) {
            stringResource(R.string.settings_amoled_on_subtitle)
        } else {
            stringResource(R.string.settings_amoled_off_subtitle)
        },
        trailing = {
            Switch(checked = isAmoled, enabled = isDarkMode, onCheckedChange = onAmoledChange)
        },
    )
    SettingsItem(
        icon = Icons.Default.Palette,
        title = stringResource(R.string.settings_accent_color),
        subtitle = when (uiState.accentColor) {
            "DEFAULT" -> stringResource(R.string.settings_accent_dynamic)
            else -> uiState.accentColor.lowercase().replaceFirstChar { it.uppercase() }
        },
    )
    SettingsAccentSelector(selectedColor = uiState.accentColor, onSelect = onAccentColorChange)
}

@Composable
private fun SettingsAccentSelector(selectedColor: String, onSelect: (String) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        accentOptions.forEach { option ->
            val selected = selectedColor == option.name ||
                (option.name == "AZURE" && selectedColor == "INDIGO")
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .selectable(
                        selected = selected,
                        role = Role.RadioButton,
                        onClick = { onSelect(option.name) },
                    )
                    .padding(4.dp)
                    .clip(CircleShape)
                    .background(
                        if (option.color == null) {
                            Brush.sweepGradient(
                                listOf(
                                    NexusColors.NexusBlue, NexusColors.Cyan400,
                                    NexusColors.AuroraGreen, NexusColors.Amber400,
                                    NexusColors.Rose400, NexusColors.NexusBlue,
                                ),
                            )
                        } else {
                            Brush.linearGradient(listOf(Color(option.color), Color(option.color)))
                        }
                    )
                    .then(
                        if (selected) Modifier.border(
                            width = 2.dp,
                            color = MaterialTheme.colorScheme.onSurface,
                            shape = CircleShape,
                        ) else Modifier
                    ),
            )
        }
    }
}

@Composable
private fun SettingsNotificationsSection(enabled: Boolean, onEnabledChange: (Boolean) -> Unit) {
    Spacer(modifier = Modifier.height(24.dp))
    SettingsSectionHeader(title = stringResource(R.string.settings_notifications))
    SettingsItem(
        icon = Icons.Default.Notifications,
        title = stringResource(R.string.settings_push_notifications),
        subtitle = stringResource(R.string.settings_push_notifications_subtitle),
        trailing = {
            Switch(checked = enabled, onCheckedChange = onEnabledChange)
        },
    )
}

@Composable
private fun SettingsConnectionsSection(
    uiState: SettingsUiState,
    onSaveGithub: (String) -> Unit,
    onSaveLeetcode: (String) -> Unit,
    onSaveCodeforces: (String) -> Unit,
    onSaveLinkedin: (String) -> Unit,
) {
    Spacer(modifier = Modifier.height(24.dp))
    SettingsSectionHeader(
        title = stringResource(R.string.settings_connections),
        caption = stringResource(R.string.settings_connections_caption),
    )
    ConnectField(
        savedValue = uiState.githubUsername,
        label = stringResource(R.string.settings_github_label),
        supporting = stringResource(R.string.settings_github_support),
        onSave = onSaveGithub,
    )
    ConnectField(
        savedValue = uiState.leetcodeUsername,
        label = stringResource(R.string.settings_leetcode_label),
        supporting = stringResource(R.string.settings_leetcode_support),
        onSave = onSaveLeetcode,
    )
    ConnectField(
        savedValue = uiState.codeforcesUsername,
        label = stringResource(R.string.settings_codeforces_label),
        supporting = stringResource(R.string.settings_codeforces_support),
        onSave = onSaveCodeforces,
    )
    ConnectField(
        savedValue = uiState.linkedinUrl,
        label = stringResource(R.string.settings_linkedin_label),
        supporting = stringResource(R.string.settings_linkedin_support),
        onSave = onSaveLinkedin,
    )
}

@Composable
private fun SettingsDataRightsSection(
    exportInFlight: Boolean,
    onPrivacyClick: () -> Unit,
    onShareHelpClick: () -> Unit,
    onExportClick: () -> Unit,
    onDeleteClick: () -> Unit,
) {
    Spacer(modifier = Modifier.height(24.dp))
    SettingsSectionHeader(title = stringResource(R.string.settings_account))
    SettingsItem(
        icon = Icons.Default.Shield,
        title = stringResource(R.string.settings_privacy_title),
        subtitle = stringResource(R.string.settings_privacy_subtitle),
        onClick = onPrivacyClick,
    )
    SettingsItem(
        icon = Icons.Default.IosShare,
        title = stringResource(R.string.settings_share_help_title),
        subtitle = stringResource(R.string.settings_share_help_subtitle),
        onClick = onShareHelpClick,
    )
    SettingsItem(
        icon = Icons.Default.Download,
        title = stringResource(R.string.settings_export_data),
        subtitle = stringResource(R.string.settings_export_subtitle),
        trailing = if (exportInFlight) {
            {
                androidx.compose.material3.CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp,
                )
            }
        } else {
            null
        },
        onClick = onExportClick,
    )
    SettingsItem(
        icon = Icons.Default.Delete,
        title = stringResource(R.string.settings_delete_account),
        subtitle = stringResource(R.string.settings_delete_subtitle),
        iconTint = MaterialTheme.colorScheme.error,
        onClick = onDeleteClick,
    )
}

@Composable
private fun SettingsHelpSection(
    versionName: String,
    onWalkthroughClick: () -> Unit,
    onReplayTourClick: () -> Unit,
) {
    SettingsItem(
        icon = Icons.Default.Info,
        title = stringResource(R.string.settings_about),
        subtitle = stringResource(R.string.settings_version, versionName),
    )
    SettingsItem(
        icon = Icons.Default.PlayCircleOutline,
        title = stringResource(R.string.settings_watch_walkthrough),
        subtitle = stringResource(R.string.settings_watch_walkthrough_subtitle),
        onClick = onWalkthroughClick,
    )
    SettingsItem(
        icon = Icons.Default.HelpOutline,
        title = stringResource(R.string.settings_replay_tour),
        subtitle = stringResource(R.string.settings_replay_tour_subtitle),
        onClick = onReplayTourClick,
    )
}

@Composable
private fun SettingsSectionHeader(title: String, caption: String? = null) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(bottom = 8.dp),
    )
    if (caption != null) {
        Text(
            text = caption,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 12.dp),
        )
    }
}

@Composable
private fun SettingsShareHelpDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.settings_share_help_title)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(stringResource(R.string.settings_share_help_intro))
                Text(stringResource(R.string.settings_share_help_step_1))
                Text(stringResource(R.string.settings_share_help_step_2))
                Text(stringResource(R.string.settings_share_help_step_3))
                Text(
                    stringResource(R.string.settings_share_help_footnote),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.settings_share_help_done))
            }
        },
    )
}

@Composable
private fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    trailing: @Composable (() -> Unit)? = null,
    onClick: (() -> Unit)? = null,
    /** Overrides the default muted icon tint — used only for destructive actions. */
    iconTint: androidx.compose.ui.graphics.Color? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint ?: MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (trailing != null) {
            trailing()
        }
    }
}

/**
 * One connected-platform field. The draft re-seeds from the saved value, a
 * check appears only when the draft differs, and saving clears it — no
 * separate edit screen, no save bar.
 */
@Composable
private fun ConnectField(
    savedValue: String,
    label: String,
    supporting: String,
    onSave: (String) -> Unit,
) {
    var draft by remember(savedValue) { mutableStateOf(savedValue) }
    val dirty = draft.trim() != savedValue.trim()
    NexusTextField(
        value = draft,
        onValueChange = { draft = it },
        label = label,
        supportingText = supporting,
        singleLine = true,
        trailingIcon = {
            if (dirty) {
                NexusIconButton(
                    icon = Icons.Default.Check,
                    contentDescription = stringResource(R.string.settings_connection_save),
                    onClick = { onSave(draft.trim()) },
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
        },
        modifier = Modifier.fillMaxWidth(),
    )
    Spacer(modifier = Modifier.height(8.dp))
}
