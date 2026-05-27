package com.nexus.achievements

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nexus.designsystem.component.NexusCard
import com.nexus.designsystem.theme.NexusColors
import com.nexus.designsystem.theme.NexusGradients
import kotlinx.collections.immutable.persistentListOf
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

data class AchievementBadge(
    val id: String,
    val emoji: String,
    val title: String,
    val isUnlocked: Boolean,
)

data class RecentUnlock(
    val badgeId: String,
    val title: String,
    val emoji: String,
    val timestamp: String,
)

private val sampleBadges = persistentListOf(
    AchievementBadge("1", "\uD83D\uDD25", "First Streak", true),
    AchievementBadge("2", "\uD83D\uDCAA", "10 Applications", true),
    AchievementBadge("3", "\uD83C\uDF1F", "Certification Star", true),
    AchievementBadge("4", "\uD83D\uDE80", "Super Streak", true),
    AchievementBadge("5", "\uD83C\uDFAF", "Interview Ready", true),
    AchievementBadge("6", "\uD83C\uDFC6", "Master Coder", true),
    AchievementBadge("7", "\u26A1", "Speed Runner", false),
    AchievementBadge("8", "\uD83D\uDCDA", "Scholar", false),
    AchievementBadge("9", "\uD83E\uDD1D", "Networker", false),
    AchievementBadge("10", "\uD83C\uDFA8", "Portfolio Pro", false),
    AchievementBadge("11", "\uD83E\uDDE0", "AI Master", false),
    AchievementBadge("12", "\uD83D\uDCBC", "Intern Achiever", false),
)

private val sampleRecentUnlocks = persistentListOf(
    RecentUnlock("1", "First Streak", "\uD83D\uDD25", "2h ago"),
    RecentUnlock("2", "10 Applications", "\uD83D\uDCAA", "1d ago"),
    RecentUnlock("3", "Certification Star", "\uD83C\uDF1F", "3d ago"),
    RecentUnlock("4", "Super Streak", "\uD83D\uDE80", "5d ago"),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AchievementsScreen(
    onBack: () -> Unit = {},
    onSettings: () -> Unit = {},
    onLeaderboard: () -> Unit = {},
) {
    val animatedXp by animateFloatAsState(
        targetValue = 2450f / 4000f,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 100f),
        label = "xpProgress",
    )

    val animatedScore by animateFloatAsState(
        targetValue = 847f / 1000f,
        animationSpec = tween(1500),
        label = "scoreArc",
    )

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            TopAppBar(
                title = { Text("Achievements", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = onSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                modifier = Modifier.padding(horizontal = (-12).dp),
            )
        }

        item {
            LevelXpCard(
                level = 3,
                title = "Engineer",
                currentXp = 2450,
                xpForNext = 4000,
                animatedProgress = animatedXp,
            )
        }

        item {
            NexusScoreCard(
                score = 847,
                weeklyChange = 12,
                percentile = 8,
                animatedProgress = animatedScore,
            )
        }

        item {
            BadgeGallery(badges = sampleBadges)
        }

        item {
            StatsRow()
        }

        item {
            RecentUnlocksSection(unlocks = sampleRecentUnlocks)
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun LevelXpCard(
    level: Int,
    title: String,
    currentXp: Int,
    xpForNext: Int,
    animatedProgress: Float,
) {
    NexusCard(
        modifier = Modifier.fillMaxWidth(),
        gradient = NexusGradients.PrimaryDeep,
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.2f))
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "L$level $title",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                        )
                    }
                }
                Text(
                    text = "\uD83D\uDE80",
                    fontSize = 28.sp,
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "$currentXp / $xpForNext XP",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.8f),
            )

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp)),
                color = Color.White,
                trackColor = Color.White.copy(alpha = 0.2f),
                strokeCap = StrokeCap.Round,
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "${xpForNext - currentXp} XP to next level",
                style = MaterialTheme.typography.labelSmall,
                color = Color.White.copy(alpha = 0.6f),
            )
        }
    }
}

@Composable
private fun NexusScoreCard(
    score: Int,
    weeklyChange: Int,
    percentile: Int,
    animatedProgress: Float,
) {
    NexusCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier.size(100.dp),
                contentAlignment = Alignment.Center,
            ) {
                Canvas(modifier = Modifier.size(100.dp)) {
                    val sweepAngle = animatedProgress * 270f
                    val arcPadding = 12f
                    val arcRadius = (size.minDimension - arcPadding) / 2
                    val topLeft = Offset(
                        (size.width - arcRadius * 2) / 2,
                        (size.height - arcRadius * 2) / 2,
                    )

                    drawArc(
                        color = NexusColors.Neutral700,
                        startAngle = 135f,
                        sweepAngle = 270f,
                        useCenter = false,
                        topLeft = topLeft,
                        size = androidx.compose.ui.geometry.Size(arcRadius * 2, arcRadius * 2),
                        style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round),
                    )

                    drawArc(
                        brush = Brush.sweepGradient(
                            listOf(NexusColors.Indigo500, NexusColors.Cyan400),
                            center = androidx.compose.ui.geometry.Offset(size.width / 2, size.height / 2),
                        ),
                        startAngle = 135f,
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        topLeft = topLeft,
                        size = androidx.compose.ui.geometry.Size(arcRadius * 2, arcRadius * 2),
                        style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round),
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "$score",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = NexusColors.Indigo400,
                    )
                    Text(
                        text = "NEXUS",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = "NEXUS Score",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "\u2191 +$weeklyChange this week",
                    style = MaterialTheme.typography.bodySmall,
                    color = NexusColors.Emerald400,
                    fontWeight = FontWeight.Medium,
                )
                Text(
                    text = "Top $percentile% of users",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun BadgeGallery(badges: List<AchievementBadge>) {
    Column {
        Text(
            text = "Badges",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 12.dp),
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.height(400.dp),
        ) {
            items(badges, key = { it.id }) { badge ->
                BadgeItem(badge = badge)
            }
        }
    }
}

@Composable
private fun BadgeItem(badge: AchievementBadge) {
    NexusCard(
        modifier = Modifier.fillMaxWidth(),
        glassEffect = !badge.isUnlocked,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(
                        if (badge.isUnlocked) {
                            Brush.linearGradient(listOf(NexusColors.Indigo500, NexusColors.Cyan400))
                        } else {
                            SolidColor(NexusColors.Neutral700)
                        }
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = if (badge.isUnlocked) badge.emoji else "\uD83D\uDD12",
                    fontSize = 24.sp,
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = badge.title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (badge.isUnlocked) FontWeight.SemiBold else FontWeight.Normal,
                color = if (badge.isUnlocked) MaterialTheme.colorScheme.onSurface
                else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                textAlign = TextAlign.Center,
                maxLines = 1,
            )

            if (!badge.isUnlocked) {
                Text(
                    text = "Locked",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                )
            }
        }
    }
}

@Composable
private fun StatsRow() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        StatCardSmall(value = "12,450", label = "XP Earned", emoji = "\u26A1", modifier = Modifier.weight(1f))
        StatCardSmall(value = "6", label = "Badges", emoji = "\uD83C\uDFC6", modifier = Modifier.weight(1f))
        StatCardSmall(value = "12", label = "Streak", emoji = "\uD83D\uDD25", modifier = Modifier.weight(1f))
    }
}

@Composable
private fun StatCardSmall(
    value: String,
    label: String,
    emoji: String,
    modifier: Modifier = Modifier,
) {
    NexusCard(modifier = modifier) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = emoji, fontSize = 24.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun RecentUnlocksSection(unlocks: List<RecentUnlock>) {
    Column {
        Text(
            text = "Recent Unlocks",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 12.dp),
        )

        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(unlocks, key = { it.badgeId }) { unlock ->
                RecentUnlockCard(unlock = unlock)
            }
        }
    }
}

@Composable
private fun RecentUnlockCard(unlock: RecentUnlock) {
    NexusCard(
        modifier = Modifier.width(140.dp),
        glassEffect = true,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = unlock.emoji, fontSize = 32.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = unlock.title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                maxLines = 1,
            )
            Text(
                text = unlock.timestamp,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
