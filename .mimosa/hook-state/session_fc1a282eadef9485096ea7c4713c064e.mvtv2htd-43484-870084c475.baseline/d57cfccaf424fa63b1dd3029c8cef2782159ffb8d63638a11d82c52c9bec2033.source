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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nexus.designsystem.component.GlassCard
import com.nexus.designsystem.component.LoadingState
import com.nexus.designsystem.component.NexusButton
import com.nexus.designsystem.component.NexusTopBar
import com.nexus.designsystem.component.SectionHeader
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

    Scaffold(
        topBar = {
            NexusTopBar(
                title = stringResource(R.string.community_community),
                onBack = onBack,
                actions = {
                    IconButton(onClick = viewModel::refresh, enabled = !isRefreshing) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = stringResource(R.string.community_refresh),
                        )
                    }
                },
            )
        },
    ) { padding ->
        if (uiState.isLoading) {
            LoadingState(
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
) {
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
                        text = if (uiState.college.isBlank()) {
                            stringResource(R.string.community_college_missing)
                        } else {
                            stringResource(R.string.community_college, uiState.college)
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

        if (uiState.hackathons.isEmpty()) {
            item {
                GlassCard {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            text = stringResource(R.string.community_no_open_hackathons),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(Modifier.height(12.dp))
                        NexusButton(
                            text = stringResource(R.string.community_refresh),
                            onClick = onRefresh,
                        )
                    }
                }
            }
        } else {
            items(uiState.hackathons, key = { it.id }) { hackathon ->
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
