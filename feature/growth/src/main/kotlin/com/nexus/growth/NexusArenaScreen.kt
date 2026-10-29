package com.nexus.growth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nexus.designsystem.component.AnimatedStatCard
import com.nexus.designsystem.component.LoadingState
import com.nexus.designsystem.component.NexusCard
import com.nexus.designsystem.component.NexusTopBar
import com.nexus.designsystem.effect.StreakIgnition
import com.nexus.designsystem.theme.NexusSpacing
import com.nexus.domain.model.LeaderboardEntry

private val PodiumGold = Color(0xFFFFD700)
private val PodiumSilver = Color(0xFFC0C0C0)
private val PodiumBronze = Color(0xFFCD7F32)

@Composable
fun NexusArenaScreen(
    onBack: () -> Unit = {},
    viewModel: NexusArenaViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    NexusArenaContent(
        uiState = uiState,
        onBack = onBack,
        onRefresh = viewModel::refresh,
        onJoin = viewModel::joinLeaderboard,
        onLeave = viewModel::leaveLeaderboard,
    )
}

@Composable
fun NexusArenaContent(
    uiState: NexusArenaUiState,
    onBack: () -> Unit,
    onRefresh: () -> Unit,
    onJoin: () -> Unit,
    onLeave: () -> Unit,
) {
    val currentUser = uiState.currentUser
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = NexusSpacing.xl, vertical = NexusSpacing.xs),
        verticalArrangement = Arrangement.spacedBy(NexusSpacing.md),
    ) {
        item {
            ArenaTopBar(
                streakDays = uiState.streakDays,
                onBack = onBack,
                onRefresh = onRefresh,
            )
        }

        item {
            ArenaStats(
                totalXp = uiState.totalXp,
                streakDays = uiState.streakDays,
                currentUser = currentUser,
                topPercent = uiState.topPercent,
            )
        }

        if (!uiState.isLoading) {
            if (!uiState.isPublished) {
                item {
                    JoinLeaderboardCard(
                        isPublishing = uiState.isPublishing,
                        actionFailed = uiState.actionFailed,
                        onJoin = onJoin,
                    )
                }
            } else {
                item {
                    PublishedProfileCard(
                        isPublishing = uiState.isPublishing,
                        actionFailed = uiState.actionFailed,
                        onLeave = onLeave,
                    )
                }
            }
        }

        when {
            uiState.isLoading && uiState.entries.isEmpty() -> item {
                LoadingState(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .padding(top = NexusSpacing.sm),
                )
            }

            uiState.entries.isNotEmpty() -> {
                if (uiState.entries.size >= 3) {
                    item { Podium(uiState.entries.take(3)) }
                }
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = NexusSpacing.xs),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            stringResource(R.string.arena_global_leaderboard),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            stringResource(R.string.arena_entries, uiState.entries.size),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                val listed = if (uiState.entries.size >= 3) uiState.entries.drop(3) else uiState.entries
                items(listed, key = { it.userId }) { entry ->
                    ArenaRow(entry = entry)
                }
            }

            else -> item { ArenaEmptyState() }
        }

        item { Spacer(Modifier.height(NexusSpacing.xl)) }
    }
}

@Composable
private fun ArenaTopBar(
    streakDays: Int,
    onBack: () -> Unit,
    onRefresh: () -> Unit,
) {
    NexusTopBar(
        onBack = onBack,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (streakDays > 0) {
                    StreakIgnition(streakCount = streakDays, modifier = Modifier.size(28.dp))
                    Spacer(Modifier.width(8.dp))
                }
                Text(stringResource(R.string.growth_nexus_arena), fontWeight = FontWeight.Bold)
            }
        },
        actions = {
            IconButton(onClick = onRefresh) {
                Icon(
                    Icons.Default.Refresh,
                    contentDescription = stringResource(R.string.arena_refresh),
                )
            }
        },
    )
}

@Composable
private fun ArenaStats(
    totalXp: Int,
    streakDays: Int,
    currentUser: LeaderboardEntry?,
    topPercent: Int,
) {
    Column(verticalArrangement = Arrangement.spacedBy(NexusSpacing.sm)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(NexusSpacing.sm),
        ) {
            AnimatedStatCard(
                value = totalXp,
                label = stringResource(R.string.arena_total_xp),
                modifier = Modifier.weight(1f),
            )
            AnimatedStatCard(
                value = streakDays,
                label = stringResource(R.string.arena_streak),
                modifier = Modifier.weight(1f),
            )
        }
        if (currentUser != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(NexusSpacing.sm),
            ) {
                AnimatedStatCard(
                    value = currentUser.rank,
                    label = stringResource(R.string.arena_global_rank),
                    modifier = Modifier.weight(1f),
                )
                AnimatedStatCard(
                    value = topPercent,
                    label = stringResource(R.string.arena_top),
                    suffix = "%",
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun ArenaEmptyState() {
    NexusCard(modifier = Modifier.fillMaxWidth()) {
        Text(
            stringResource(R.string.arena_empty_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            stringResource(R.string.arena_empty_body),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun Podium(entries: List<LeaderboardEntry>) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = NexusSpacing.xs),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Bottom,
    ) {
        PodiumSpot(
            entry = entries[1],
            place = 2,
            accent = PodiumSilver,
            blockHeight = 84.dp,
            modifier = Modifier.weight(1f),
        )
        PodiumSpot(
            entry = entries[0],
            place = 1,
            accent = PodiumGold,
            blockHeight = 116.dp,
            modifier = Modifier.weight(1f),
        )
        PodiumSpot(
            entry = entries[2],
            place = 3,
            accent = PodiumBronze,
            blockHeight = 68.dp,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun PodiumSpot(
    entry: LeaderboardEntry,
    place: Int,
    accent: Color,
    blockHeight: Dp,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(Brush.linearGradient(listOf(accent.copy(alpha = 0.9f), accent.copy(alpha = 0.35f)))),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = entry.userName.trim().take(1).uppercase().ifBlank { "?" },
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1F2937),
            )
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = entry.userName,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = if (entry.isCurrentUser) FontWeight.Bold else FontWeight.Medium,
            color = if (entry.isCurrentUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = stringResource(R.string.arena_xp_value, entry.score),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(blockHeight)
                .clip(RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(accent.copy(alpha = 0.32f), accent.copy(alpha = 0.06f)),
                    )
                ),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = stringResource(R.string.arena_rank_label, place),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = accent,
            )
        }
    }
}

@Composable
private fun ArenaRow(entry: LeaderboardEntry, modifier: Modifier = Modifier) {
    val accent = podiumColor(entry.rank)
    val isUser = entry.isCurrentUser
    val surface = MaterialTheme.colorScheme.surfaceContainerHighest

    NexusCard(
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .background((accent ?: MaterialTheme.colorScheme.surfaceContainerHighest).copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = stringResource(R.string.arena_rank_label, entry.rank),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = accent ?: MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.width(10.dp))
            Box(
                modifier = Modifier.size(36.dp).clip(CircleShape)
                    .background(if (isUser) MaterialTheme.colorScheme.primary else surface),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = entry.userName.trim().take(1).uppercase().ifBlank { "?" },
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = entry.userName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (isUser) FontWeight.SemiBold else FontWeight.Normal,
                    color = if (isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (isUser) {
                    Text(
                        text = stringResource(R.string.arena_you),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
            Text(
                text = stringResource(R.string.arena_xp_value, entry.score),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = if (isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}

@Composable
private fun JoinLeaderboardCard(
    isPublishing: Boolean,
    actionFailed: Boolean,
    onJoin: () -> Unit,
) {
    NexusCard(modifier = Modifier.fillMaxWidth()) {
        Text(
            stringResource(R.string.arena_join_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            stringResource(R.string.arena_join_body),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (actionFailed) {
            Spacer(Modifier.height(8.dp))
            Text(
                stringResource(R.string.arena_join_failed),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
        }
        Spacer(Modifier.height(12.dp))
        Button(
            onClick = onJoin,
            enabled = !isPublishing,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                stringResource(
                    if (isPublishing) R.string.arena_joining else R.string.arena_join_action,
                )
            )
        }
    }
}

@Composable
private fun PublishedProfileCard(
    isPublishing: Boolean,
    actionFailed: Boolean,
    onLeave: () -> Unit,
) {
    NexusCard(modifier = Modifier.fillMaxWidth()) {
        Text(
            stringResource(R.string.arena_published_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            stringResource(R.string.arena_published_body),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (actionFailed) {
            Spacer(Modifier.height(8.dp))
            Text(
                stringResource(R.string.arena_leave_failed),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
            )
        }
        TextButton(onClick = onLeave, enabled = !isPublishing) {
            Text(stringResource(R.string.arena_leave_action))
        }
    }
}

private fun podiumColor(rank: Int): Color? = when (rank) {
    1 -> PodiumGold
    2 -> PodiumSilver
    3 -> PodiumBronze
    else -> null
}
