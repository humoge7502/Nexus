package com.nexus.community

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nexus.designsystem.component.GlassCard
import com.nexus.designsystem.component.EmptyState
import com.nexus.designsystem.component.NexusButton
import com.nexus.designsystem.component.NexusIconButton
import com.nexus.designsystem.component.NexusToastData
import com.nexus.designsystem.component.NexusToastHost
import com.nexus.designsystem.component.NexusTopBar
import com.nexus.designsystem.component.SectionHeader
import com.nexus.designsystem.component.ToastTone
import com.nexus.designsystem.component.feedback.EditorialErrorState
import com.nexus.designsystem.component.feedback.EditorialSkeletonList
import com.nexus.designsystem.component.rememberNexusToastState
import com.nexus.domain.model.Hackathon
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
fun CommunityScreen(
    onBack: () -> Unit = {},
    onOpenCohort: () -> Unit = {},
    onOpenTribe: () -> Unit = {},
    onOpenHackathon: (String) -> Unit = {},
    viewModel: CommunityViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()
    val toastState = rememberNexusToastState()

    // Resolved here because the toast is composed copy: the ViewModel says the
    // refresh hit nothing, the screen puts that in the student's language.
    val refreshFaultMessage = stringResource(R.string.community_refresh_failed)

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                CommunityEffect.RefreshFailed ->
                    toastState.show(NexusToastData(refreshFaultMessage, ToastTone.Critical))
            }
        }
    }

    Scaffold(
        topBar = {
            NexusTopBar(
                title = stringResource(R.string.community_community),
                onBack = onBack,
                actions = {
                    NexusIconButton(
                        icon = Icons.Default.Refresh,
                        contentDescription = stringResource(R.string.community_refresh),
                        onClick = viewModel::refresh,
                        enabled = !isRefreshing,
                    )
                },
            )
        },
        // The toast keeps the snackbar slot: Scaffold bottom-centers it and
        // offsets it above any bottom chrome, so only the plate changes.
        snackbarHost = { NexusToastHost(toastState) },
    ) { padding ->
        if (uiState.isLoading) {
            EditorialSkeletonList(
                count = 3,
                loadingLabel = stringResource(R.string.community_loading),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
            )
        } else {
            CommunityContent(
                uiState = uiState,
                onOpenCohort = onOpenCohort,
                onOpenTribe = onOpenTribe,
                onOpenHackathon = onOpenHackathon,
                onRefresh = viewModel::refresh,
                onRetry = viewModel::retry,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
            )
        }
    }
}

@Composable
fun CommunityContent(
    uiState: CommunityUiState,
    onOpenCohort: () -> Unit,
    onOpenTribe: () -> Unit,
    onOpenHackathon: (String) -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    onRetry: () -> Unit = {},
) {
    // Read before every branch below: a failed read carries neither hackathons
    // nor a college, and either one left to itself would be served as "you have
    // nothing" rather than as "nothing could be read".
    val loadFault = uiState.error

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            GlassCard {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = stringResource(R.string.community_your_batch),
                        style = MaterialTheme.typography.titleLarge,
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = when {
                            loadFault != null -> stringResource(R.string.community_batch_unverified)
                            uiState.college.isBlank() -> stringResource(R.string.community_college_missing)
                            else -> stringResource(R.string.community_college, uiState.college)
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.height(12.dp))
                    NexusButton(
                        text = stringResource(R.string.community_open_cohort),
                        onClick = onOpenCohort,
                    )
                }
            }
        }

        item { SectionHeader(stringResource(R.string.community_find_teammates)) }

        when {
            loadFault != null -> item {
                EditorialErrorState(
                    headline = stringResource(R.string.community_unable_to_load),
                    reason = stringResource(R.string.community_error_reason),
                    actionLabel = stringResource(R.string.community_try_again),
                    onAction = onRetry,
                    secondaryHint = loadFault.takeIf { it.isNotBlank() },
                )
            }

            uiState.hackathons.isEmpty() -> item {
                EmptyState(
                    title = stringResource(R.string.community_no_open_hackathons),
                    description = stringResource(R.string.community_no_open_hackathons_detail),
                    voice = stringResource(R.string.community_empty_voice),
                    actionLabel = stringResource(R.string.community_refresh),
                    onAction = onRefresh,
                )
            }

            else -> items(uiState.hackathons, key = { it.id }) { hackathon ->
                HackathonTeammateCard(
                    hackathon = hackathon,
                    onOpen = { onOpenHackathon(hackathon.id) },
                )
            }
        }

        item { SectionHeader(stringResource(R.string.community_peer_accountability)) }

        item {
            GlassCard {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = stringResource(R.string.community_peer_accountability_body),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Spacer(Modifier.height(12.dp))
                    NexusButton(
                        text = stringResource(R.string.community_open_tribe),
                        onClick = onOpenTribe,
                    )
                }
            }
        }

        item {
            GlassCard {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = stringResource(R.string.community_mentor_discovery_peer_accountability_and_study),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

@Composable
private fun HackathonTeammateCard(
    hackathon: Hackathon,
    onOpen: () -> Unit,
) {
    val dateFormatter = remember { DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM) }

    GlassCard {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = hackathon.title,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.weight(1f),
                )
                if (hackathon.isLive) {
                    Text(
                        text = stringResource(R.string.community_live),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier
                            .background(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = MaterialTheme.shapes.small,
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                    )
                }
            }
            Spacer(Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.community_organizer, hackathon.organizer),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = stringResource(
                    R.string.community_dates,
                    hackathon.startDate.format(dateFormatter),
                    hackathon.endDate.format(dateFormatter),
                ),
                style = MaterialTheme.typography.bodySmall,
            )
            if (hackathon.prizePool.isNotBlank()) {
                Text(
                    text = stringResource(R.string.community_prize, hackathon.prizePool),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            Text(
                text = stringResource(
                    R.string.community_team_size,
                    hackathon.teamSize.first,
                    hackathon.teamSize.last,
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(12.dp))
            NexusButton(
                text = stringResource(R.string.community_view_details),
                onClick = onOpen,
            )
        }
    }
}
