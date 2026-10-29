package com.nexus.achievements

import androidx.compose.ui.res.stringResource
import com.nexus.achievements.R

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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.material3.CircularProgressIndicator
import com.nexus.designsystem.component.NexusCard
import com.nexus.designsystem.component.NexusTopBar
import com.nexus.designsystem.theme.NexusColors
import com.nexus.designsystem.theme.NexusGradients
import com.nexus.domain.model.LeaderboardEntry
import com.nexus.domain.repository.LeaderboardTab

private val tabEntries = listOf(
    R.string.leaderboard_tab_global to LeaderboardTab.GLOBAL,
    R.string.leaderboard_tab_college to LeaderboardTab.COLLEGE,
    R.string.leaderboard_tab_friends to LeaderboardTab.FRIENDS,
    R.string.leaderboard_tab_domain_dsa to LeaderboardTab.DOMAIN_DSA,
)

@Composable
fun LeaderboardScreen(
    onBack: () -> Unit = {},
    onOpenArena: () -> Unit = {},
    viewModel: LeaderboardViewModel = androidx.hilt.navigation.compose.hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val selectedTab = tabEntries.indexOfFirst { it.second == uiState.selectedTab }.coerceAtLeast(0)

    Column(modifier = Modifier.fillMaxSize()) {
        NexusTopBar(
            title = stringResource(R.string.achievements_leaderboard),
            onBack = onBack,
        )

        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.Transparent,
            contentColor = MaterialTheme.colorScheme.primary,
            edgePadding = 20.dp,
            modifier = Modifier.padding(bottom = 8.dp),
        ) {
            tabEntries.forEachIndexed { index, (labelRes, tab) ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { viewModel.selectTab(tab) },
                    text = {
                        Text(
                            text = stringResource(labelRes),
                            fontWeight = if (selectedTab == index) FontWeight.SemiBold else FontWeight.Normal,
                        )
                    },
                )
            }
        }

        uiState.currentUser?.let { currentUser ->
            CurrentUserCard(currentUser)
        }

        Spacer(modifier = Modifier.height(12.dp))

        when {
            uiState.isLoading -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }

            uiState.entries.isEmpty() -> LeaderboardEmptyState(onOpenArena)

            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                itemsIndexed(uiState.entries, key = { _, entry -> entry.userId }) { _, entry ->
                    LeaderboardRow(entry = entry)
                }
            }
        }
    }
}

@Composable
private fun CurrentUserCard(currentUser: LeaderboardEntry) {
    NexusCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        gradient = NexusGradients.PrimaryDeep,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = currentUser.userName.take(1),
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 20.sp,
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = currentUser.userName,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                )
                Text(
                    text = stringResource(R.string.leaderboard_rank, currentUser.rank),
                    color = Color.White.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.bodySmall,
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${currentUser.score}",
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 20.sp,
                )
                Text(
                    text = stringResource(R.string.leaderboard_xp),
                    color = Color.White.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.labelSmall,
                )
            }
        }
    }
}

@Composable
private fun LeaderboardEmptyState(onOpenArena: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = stringResource(R.string.leaderboard_empty),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onOpenArena) {
            Text(stringResource(R.string.leaderboard_open_arena))
        }
    }
}

@Composable
private fun LeaderboardRow(
    entry: LeaderboardEntry,
) {
    val rankColor = when (entry.rank) {
        1 -> NexusColors.Amber400
        2 -> NexusColors.Neutral300
        3 -> NexusColors.Amber600
        else -> null
    }
    val college = entry.college

    NexusCard(
        modifier = Modifier.fillMaxWidth(),
        containerColor = if (entry.isCurrentUser) {
            MaterialTheme.colorScheme.primaryContainer
        } else null,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(rankColor?.copy(alpha = 0.16f) ?: Color.Transparent),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "#${entry.rank}",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = rankColor ?: MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surfaceContainerHighest),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = entry.userName.take(1).uppercase(),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = entry.userName,
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.bodyMedium,
                )
                if (!college.isNullOrBlank()) {
                    Text(
                        text = college,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${entry.score}",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleSmall,
                )
            }
        }
    }
}
