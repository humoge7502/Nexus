package com.nexus.achievements

import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.ProgressBarRangeInfo
import androidx.compose.ui.semantics.progressBarRangeInfo
import androidx.compose.ui.semantics.semantics
import com.nexus.achievements.R

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Handshake
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.Work
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.nexus.designsystem.component.AchievementBurst
import com.nexus.designsystem.component.NexusCard
import com.nexus.designsystem.component.NexusTopBar
import com.nexus.designsystem.theme.NexusColors
import com.nexus.designsystem.theme.NexusGradients
import com.nexus.designsystem.theme.NexusShapes
import com.nexus.designsystem.theme.NexusTypography
import com.nexus.designsystem.theme.nexus
import java.util.Locale

data class AchievementBadge(
    val id: String,
    val icon: ImageVector,
    val title: String,
    val isUnlocked: Boolean,
)

data class RecentUnlock(
    val badgeId: String,
    val title: String,
    val icon: ImageVector,
    val timestamp: String,
)



@Composable
fun AchievementsScreen(
    onBack: () -> Unit = {},
    onSettings: () -> Unit = {},
    onLeaderboard: () -> Unit = {},
    viewModel: AchievementsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val badges = (uiState.allAchievements as? com.nexus.domain.model.UiState.Success)?.data
        ?.map { AchievementBadge(it.id, badgeIcon(it.type), it.title, it.isUnlocked) }
        ?: emptyList()

    val unlocks = (uiState.unlockedAchievements as? com.nexus.domain.model.UiState.Success)?.data
        ?.filter { it.unlockedAt != null }
        ?.sortedByDescending { it.unlockedAt }
        ?.take(4)
        ?.map { RecentUnlock(it.id, it.title, badgeIcon(it.type), it.unlockedAt.toString()) }
        ?: emptyList()

    val animatedXp by animateFloatAsState(
        targetValue = (uiState.totalXP.toFloat() / uiState.xpToNextLevel.coerceAtLeast(1)).coerceIn(0f, 1f),
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 100f),
        label = "xpProgress",
    )

    val animatedScore by animateFloatAsState(
        targetValue = (uiState.nexusScore / 1000f).coerceIn(0f, 1f),
        animationSpec = tween(1500),
        label = "scoreArc",
    )

    Box(modifier = Modifier.fillMaxSize()) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            NexusTopBar(
                title = stringResource(R.string.achievements_achievements),
                onBack = onBack,
                actions = {
                    IconButton(onClick = onSettings) {
                        Icon(Icons.Default.Settings, contentDescription = stringResource(R.string.cd_settings))
                    }
                },
            )
        }

        item {
            LevelXpCard(
                level = uiState.level,
                title = uiState.levelName,
                currentXp = uiState.totalXP,
                xpForNext = uiState.xpToNextLevel,
                animatedProgress = animatedXp,
            )
        }

        item {
            NexusScoreCard(
                score = uiState.nexusScore,
                weeklyChange = uiState.weeklyChange,
                percentile = uiState.percentile,
                animatedProgress = animatedScore,
            )
        }

        item {
            BadgeGallery(badges = badges)
        }

        item {
            StatsRow(
                totalXp = uiState.totalXP,
                badgesUnlocked = uiState.unlockedBadgeCount,
                streakDays = uiState.streakDays,
            )
        }

        if (unlocks.isNotEmpty()) {
            item {
                RecentUnlocksSection(unlocks = unlocks)
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }

        uiState.celebration?.let { name ->
            AchievementBurst(badgeName = name, onDismiss = viewModel::dismissCelebration)
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
        gradient = NexusGradients.Primary,
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = stringResource(R.string.achievements_level_title, level, title)
                        .uppercase(Locale.ROOT),
                    style = NexusTypography.DataLabel,
                    color = NexusColors.SignalInk.copy(alpha = 0.86f),
                )
                Icon(
                    Icons.Default.RocketLaunch,
                    contentDescription = null,
                    tint = NexusColors.SignalInk.copy(alpha = 0.86f),
                    modifier = Modifier.size(20.dp),
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = currentXp.toString(),
                    style = NexusTypography.TelemetryDisplay,
                    color = NexusColors.SignalInk,
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = stringResource(R.string.achievements_xp_of_total, xpForNext),
                    style = NexusTypography.DataLabel,
                    color = NexusColors.SignalInk.copy(alpha = 0.86f),
                    modifier = Modifier.padding(bottom = 6.dp),
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(NexusShapes.tag),
                color = NexusColors.SignalInk,
                trackColor = NexusColors.SignalInk.copy(alpha = 0.18f),
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(
                    R.string.achievements_xp_to_next,
                    (xpForNext - currentXp).coerceAtLeast(0),
                ),
                style = MaterialTheme.typography.labelSmall,
                color = NexusColors.SignalInk.copy(alpha = 0.86f),
            )
        }
    }
}

@Composable
private fun NexusScoreCard(
    score: Int,
    weeklyChange: Int,
    percentile: Int?,
    animatedProgress: Float,
) {
    NexusCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .semantics {
                        progressBarRangeInfo = ProgressBarRangeInfo(
                            score.toFloat().coerceIn(0f, 1000f),
                            0f..1000f,
                        )
                    },
                contentAlignment = Alignment.Center,
            ) {
                val trackColor = MaterialTheme.colorScheme.surfaceContainerHighest
                val arcColor = MaterialTheme.colorScheme.primary
                val arcTail = MaterialTheme.nexus.structure
                Canvas(modifier = Modifier.size(100.dp)) {
                    val sweepAngle = animatedProgress * 270f
                    val arcPadding = 12f
                    val arcRadius = (size.minDimension - arcPadding) / 2
                    val topLeft = Offset(
                        (size.width - arcRadius * 2) / 2,
                        (size.height - arcRadius * 2) / 2,
                    )

                    drawArc(
                        color = trackColor,
                        startAngle = 135f,
                        sweepAngle = 270f,
                        useCenter = false,
                        topLeft = topLeft,
                        size = androidx.compose.ui.geometry.Size(arcRadius * 2, arcRadius * 2),
                        style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round),
                    )

                    drawArc(
                        brush = Brush.sweepGradient(
                            listOf(arcColor, arcTail),
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
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Text(text = stringResource(R.string.text_nexus),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(text = stringResource(R.string.text_nexus_score),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = when {
                        weeklyChange > 0 -> stringResource(
                            R.string.achievements_week_up,
                            weeklyChange,
                        )
                        weeklyChange < 0 -> stringResource(
                            R.string.achievements_week_down,
                            weeklyChange,
                        )
                        else -> stringResource(R.string.achievements_week_none)
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = when {
                        weeklyChange > 0 -> MaterialTheme.nexus.positive
                        weeklyChange < 0 -> MaterialTheme.colorScheme.error
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    fontWeight = FontWeight.Medium,
                )
                Text(
                    text = percentile?.let {
                        stringResource(R.string.achievements_top_percentile, it)
                    } ?: stringResource(R.string.achievements_percentile_unavailable),
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
        Text(text = stringResource(R.string.text_badges),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(bottom = 12.dp),
        )

        if (badges.isEmpty()) {
            Text(text = stringResource(R.string.text_badges_unlock_as_you_apply_learn_and_keep_your),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 8.dp),
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                badges.chunked(2).forEach { rowBadges ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        rowBadges.forEach { badge ->
                            BadgeItem(badge = badge, modifier = Modifier.weight(1f))
                        }
                        if (rowBadges.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BadgeItem(badge: AchievementBadge, modifier: Modifier = Modifier) {
    NexusCard(
        modifier = modifier.fillMaxWidth(),
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
                        if (badge.isUnlocked) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surfaceContainerHighest
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = if (badge.isUnlocked) badge.icon else Icons.Default.Lock,
                    contentDescription = null,
                    tint = if (badge.isUnlocked) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.size(26.dp),
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
                Text(text = stringResource(R.string.text_locked),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                )
            }
        }
    }
}

@Composable
private fun StatsRow(totalXp: Int, badgesUnlocked: Int, streakDays: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        StatCardSmall(
            value = "%,d".format(totalXp),
            label = stringResource(R.string.achievements_xp_earned),
            icon = Icons.Default.Bolt,
            modifier = Modifier.weight(1f),
        )
        StatCardSmall(
            value = badgesUnlocked.toString(),
            label = stringResource(R.string.text_badges),
            icon = Icons.Default.EmojiEvents,
            modifier = Modifier.weight(1f),
        )
        StatCardSmall(
            value = streakDays.toString(),
            label = stringResource(R.string.achievements_day_streak),
            icon = Icons.Default.LocalFireDepartment,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun StatCardSmall(
    value: String,
    label: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
) {
    NexusCard(modifier = modifier) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp),
            )
            Spacer(modifier = Modifier.height(6.dp))
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
        Text(text = stringResource(R.string.text_recent_unlocks),
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
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = unlock.icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp),
            )
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

private fun badgeIcon(type: com.nexus.domain.model.AchievementType): ImageVector = when (type) {
    com.nexus.domain.model.AchievementType.FIRST_INTERNSHIP -> Icons.Default.Work
    com.nexus.domain.model.AchievementType.FIRST_CERTIFICATION -> Icons.Default.WorkspacePremium
    com.nexus.domain.model.AchievementType.STREAK_7 -> Icons.Default.LocalFireDepartment
    com.nexus.domain.model.AchievementType.STREAK_30 -> Icons.Default.RocketLaunch
    com.nexus.domain.model.AchievementType.TEN_APPLICATIONS -> Icons.Default.Send
    com.nexus.domain.model.AchievementType.FIVE_CERTS -> Icons.Default.MilitaryTech
    com.nexus.domain.model.AchievementType.MASTER_CODER -> Icons.Default.EmojiEvents
    com.nexus.domain.model.AchievementType.NETWORKER -> Icons.Default.Handshake
    com.nexus.domain.model.AchievementType.EARLY_BIRD -> Icons.Default.WbSunny
    com.nexus.domain.model.AchievementType.NIGHT_OWL -> Icons.Default.DarkMode
}
