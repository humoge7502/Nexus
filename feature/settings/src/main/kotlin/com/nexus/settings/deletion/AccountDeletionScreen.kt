package com.nexus.settings.deletion

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nexus.designsystem.component.NexusButton
import com.nexus.designsystem.component.NexusCard
import com.nexus.designsystem.component.NexusOutlineButton
import com.nexus.designsystem.component.NexusTextField
import com.nexus.designsystem.component.feedback.EditorialStatusBand
import com.nexus.designsystem.component.feedback.StatusTone
import com.nexus.settings.R

@Composable
fun AccountDeletionScreen(
    onNavigateBack: () -> Unit,
    onDeletionComplete: () -> Unit,
    viewModel: AccountDeletionViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var confirmText by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is AccountDeletionEffect.DeletionComplete -> onDeletionComplete()
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            imageVector = Icons.Filled.Warning,
            contentDescription = stringResource(R.string.cd_warning),
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(48.dp)
        )

        Text(
            stringResource(R.string.delete_title),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.error,
            fontWeight = FontWeight.Bold,
        )

        NexusCard {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(stringResource(R.string.settings_this_will_permanently_delete),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(Modifier.height(8.dp))

                listOf(
                    "Your Sagitta profile and career score",
                    "All saved opportunities and applications",
                    "AI chat history and conversations",
                    "Goals and progress tracking",
                    "Achievements and badges",
                    "Portfolio projects",
                    "Uploaded resume files",
                ).forEach { item ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.Close,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(text = item, style = MaterialTheme.typography.bodyMedium)
                    }
                    Spacer(Modifier.height(4.dp))
                }
            }
        }

        Text(
            stringResource(R.string.delete_warning),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error,
        )

        NexusTextField(
            value = confirmText,
            onValueChange = { confirmText = it },
            label = stringResource(R.string.delete_confirm_hint),
            modifier = Modifier.fillMaxWidth(),
        )

        if (uiState.requiresReauth) {
            EditorialStatusBand(
                label = stringResource(R.string.settings_sign_in_again_to_delete),
                detail = stringResource(R.string.settings_reauth_reason),
                tone = StatusTone.Fault,
            )
        }

        uiState.error?.let { error ->
            Text(text = error, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
        }

        Spacer(Modifier.weight(1f))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            NexusOutlineButton(
                text = stringResource(R.string.delete_cancel),
                onClick = onNavigateBack,
                modifier = Modifier.weight(1f),
                expand = false,
            )

            NexusButton(
                text = stringResource(R.string.delete_button),
                onClick = { viewModel.confirmDeletion() },
                enabled = confirmText == "DELETE",
                loading = uiState.isDeleting,
                gradient = listOf(MaterialTheme.colorScheme.error),
                modifier = Modifier.weight(1f),
                expand = false,
            )
        }
    }
}
