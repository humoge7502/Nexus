package com.nexus.achievements

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.material3.CircularProgressIndicator
import com.nexus.designsystem.component.NexusCard
import com.nexus.designsystem.theme.NexusColors
import com.nexus.designsystem.theme.NexusGradients
import com.nexus.domain.model.LeaderboardEntry
import com.nexus.domain.repository.LeaderboardTab
import kotlinx.collections.immutable.persistentListOf

private val tabs = persistentListOf("Global", "College", "Friends", "Domain: DSA")


private val tabEntries = persistentListOf(
    "Global" to LeaderboardTab.GLOBAL,
    "College" to LeaderboardTab.COLLEGE,
    "Friends" to LeaderboardTab.FRIENDS,
    "Domain: DSA" to LeaderboardTab.DOMAIN_DSA,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaderboardScreen(
    onBack: () -> Unit = {},
    viewModel: LeaderboardViewModel = androidx.hilt.navigation.compose.hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val selectedTab = tabEntries.indexOfFirst { it.second == uiState.selectedTab }.coerceAtLeast(0)

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Leaderboard", fontWeight = FontWeight.Bold) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
        )

        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.Transparent,
            contentColor = NexusColors.Indigo400,
            edgePadding = 20.dp,
            modifier = Modifier.padding(bottom = 8.dp),
        ) {
            tabEntries.forEachIndexed { index, (title, tab) ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { viewModel.selectTab(tab) },
                    text = {
                        Text(
                            text = title,
                            fontWeight = if (selectedTab == index) FontWeight.SemiBold else FontWeight.Normal,
                        )
                    },
                )
            }
        }

        val currentUser = uiState.currentUser

        if (currentUser != null) NexusCard(
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
                        text = "Rank #${currentUser.rank}",
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
                        text = "\u2191 +12",
                        color = NexusColors.Emerald400,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Medium,
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        when {
            uiState.isLoading -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = NexusColors.Indigo400)
            }

            uiState.entries.isEmpty() -> Box(
                modifier = Modifier.fillMaxSize().padding(32.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "No one here yet.\nEarn XP and claim Rank #1 on this leaderboard!",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                itemsIndexed(uiState.entries, key = { _, entry -> entry.userId }) { index, entry ->
                    LeaderboardRow(
                        entry = entry,
                        isTop3 = index < 3,
                    )
                }
            }
        }
    }
}

@Composable
private fun LeaderboardRow(
    entry: LeaderboardEntry,
    isTop3: Boolean,
) {
    val rankEmoji = when (entry.rank) {
        1 -> "\uD83E\uDD47"
        2 -> "\uD83E\uDD48"
        3 -> "\uD83E\uDD49"
        else -> null
    }

    val bgBrush = if (entry.isCurrentUser) {
        Brush.horizontalGradient(
            listOf(
                NexusColors.Indigo500.copy(alpha = 0.15f),
                Color.Transparent,
            )
        )
    } else null

    NexusCard(
        modifier = Modifier.fillMaxWidth(),
        glassEffect = !entry.isCurrentUser,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (bgBrush != null) Modifier.background(
                        brush = bgBrush,
                        shape = RoundedCornerShape(16.dp),
                    ) else Modifier
                ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (rankEmoji != null) {
                Text(text = rankEmoji, fontSize = 28.sp)
            } else {
                Box(
                    modifier = Modifier.size(32.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "#${entry.rank}",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        if (isTop3) {
                            Brush.linearGradient(
                                listOf(
                                    NexusColors.Amber500,
                                    NexusColors.Amber400,
                                )
                            )
                        } else SolidColor(NexusColors.Neutral700)
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = entry.userName.take(1),
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = entry.userName,
                    fontWeight = FontWeight.SemiBold,
                    style = MaterialTheme.typography.bodyMedium,
                )
                Text(
                    text = "NEXUS University",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
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
