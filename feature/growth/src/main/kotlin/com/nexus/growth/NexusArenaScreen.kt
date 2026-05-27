package com.nexus.growth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
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
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.nexus.designsystem.component.AnimatedStatCard
import com.nexus.designsystem.component.NexusCard
import com.nexus.designsystem.component.NexusChip
import com.nexus.designsystem.effect.StreakIgnition
import com.nexus.designsystem.theme.NexusColors
import com.nexus.designsystem.theme.NexusGradients
import com.nexus.designsystem.theme.NexusShapes
import com.nexus.designsystem.theme.NexusSpacing

private data class ArenaCompetition(
    val id: String,
    val name: String,
    val icon: ImageVector,
    val gradient: List<Color>,
    val participants: Int,
    val deadline: String,
    val isJoined: Boolean = false,
    val rank: Int = 0,
)

private val weeklyArenas = listOf(
    ArenaCompetition("dsa", "DSA Arena", Icons.Default.EmojiEvents, NexusGradients.Primary, 234, "6d left"),
    ArenaCompetition("learning", "Learning Arena", Icons.Default.Schedule, NexusGradients.Ocean, 156, "5d left"),
    ArenaCompetition("apply", "Application Arena", Icons.Default.Groups, NexusGradients.Success, 89, "4d left"),
)

private data class ArenaLeaderboardEntry(
    val rank: Int, val name: String, val score: Int, val isUser: Boolean = false
)

private val leaderboard = listOf(
    ArenaLeaderboardEntry(1, "Priya S.", 2840),
    ArenaLeaderboardEntry(2, "Arjun K.", 2710),
    ArenaLeaderboardEntry(3, "Nitya R.", 2590),
    ArenaLeaderboardEntry(4, "Rahul V.", 2450),
    ArenaLeaderboardEntry(5, "You", 2380, isUser = true),
    ArenaLeaderboardEntry(6, "Sneha M.", 2210),
    ArenaLeaderboardEntry(7, "Vikram P.", 2100),
    ArenaLeaderboardEntry(8, "Divya T.", 1980),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NexusArenaScreen(onBack: () -> Unit = {}) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = NexusSpacing.xl, vertical = NexusSpacing.xs),
        verticalArrangement = Arrangement.spacedBy(NexusSpacing.md),
    ) {
        item {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        StreakIgnition(streakCount = 1, modifier = Modifier.size(28.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("NEXUS Arena", fontWeight = FontWeight.Bold)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(NexusSpacing.sm),
            ) {
                AnimatedStatCard(value = 2380, label = "Your Score",
                    gradient = NexusGradients.Stellar, modifier = Modifier.weight(1f))
                AnimatedStatCard(value = 5, label = "Rank",
                    gradient = NexusGradients.Primary, modifier = Modifier.weight(1f))
                AnimatedStatCard(value = 89, label = "Percentile",
                    gradient = NexusGradients.Ocean, suffix = "th", modifier = Modifier.weight(1f))
            }
        }

        item {
            Text("Active Arenas", style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold)
        }

        items(weeklyArenas) { arena ->
            ArenaCard(arena = arena)
        }

        item {
            Spacer(Modifier.height(NexusSpacing.xs))
            Text("Leaderboard", style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold)
        }

        items(leaderboard) { entry ->
            LeaderboardRow(entry = entry)
        }

        item { Spacer(Modifier.height(NexusSpacing.xl)) }
    }
}

@Composable
private fun ArenaCard(arena: ArenaCompetition) {
    NexusCard(
        shape = NexusShapes.md,
        gradient = listOf(arena.gradient[0].copy(alpha = 0.1f), Color.Transparent),
        glassEffect = true,
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier.size(44.dp).clip(RoundedCornerShape(14.dp))
                    .background(Brush.linearGradient(arena.gradient), alpha = 0.15f),
                contentAlignment = Alignment.Center,
            ) {
                Icon(arena.icon, contentDescription = null,
                    tint = arena.gradient[0], modifier = Modifier.size(24.dp))
            }
            Spacer(Modifier.width(NexusSpacing.sm))
            Column(modifier = Modifier.weight(1f)) {
                Text(arena.name, style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold)
                Row {
                    Text("${arena.participants} participants",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(" \u00B7 ", style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(arena.deadline, style = MaterialTheme.typography.labelSmall,
                        color = if (arena.deadline.startsWith("1")) Color(0xFFEF4444)
                        else MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            Box(
                modifier = Modifier.clip(RoundedCornerShape(12.dp))
                    .background(
                        if (arena.isJoined) arena.gradient[0].copy(alpha = 0.15f)
                        else MaterialTheme.colorScheme.surfaceVariant
                    )
                    .padding(horizontal = 14.dp, vertical = 8.dp),
            ) {
                Text(
                    if (arena.isJoined) "Joined" else "Join",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (arena.isJoined) arena.gradient[0]
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun LeaderboardRow(entry: ArenaLeaderboardEntry) {
    val bg = if (entry.isUser) NexusColors.Indigo500.copy(alpha = 0.08f) else Color.Transparent
    val shape = RoundedCornerShape(12.dp)

    Row(
        modifier = Modifier.fillMaxWidth().clip(shape).background(bg)
            .padding(horizontal = NexusSpacing.sm, vertical = NexusSpacing.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.size(32.dp).clip(CircleShape)
                .background(
                    when (entry.rank) {
                        1 -> Color(0xFFFFD700).copy(alpha = 0.2f)
                        2 -> Color(0xFFC0C0C0).copy(alpha = 0.2f)
                        3 -> Color(0xFFCD7F32).copy(alpha = 0.2f)
                        else -> Color.Transparent
                    }
                ),
            contentAlignment = Alignment.Center,
        ) {
            Text("${entry.rank}", style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = when (entry.rank) {
                    1 -> Color(0xFFFFD700); 2 -> Color(0xFFC0C0C0); 3 -> Color(0xFFCD7F32)
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                })
        }
        Spacer(Modifier.width(NexusSpacing.sm))
        Column(modifier = Modifier.weight(1f)) {
            Text(entry.name, style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (entry.isUser) FontWeight.SemiBold else FontWeight.Normal,
                color = if (entry.isUser) NexusColors.Indigo400 else MaterialTheme.colorScheme.onSurface)
        }
        Text("${entry.score}", style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = if (entry.isUser) NexusColors.Indigo400 else MaterialTheme.colorScheme.onSurface)
    }
}
