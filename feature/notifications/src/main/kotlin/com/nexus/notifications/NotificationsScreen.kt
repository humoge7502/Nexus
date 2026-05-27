package com.nexus.notifications

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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.nexus.designsystem.component.EmptyState
import com.nexus.designsystem.component.NexusCard
import com.nexus.designsystem.theme.NexusColors
import com.nexus.designsystem.theme.NexusGradients
import com.nexus.designsystem.theme.NexusShapes
import com.nexus.domain.model.NotificationItem
import com.nexus.domain.model.NotificationType

data class NotificationDisplay(
    val id: String,
    val emoji: String,
    val title: String,
    val body: String,
    val time: String,
    val type: NotificationType,
    val isRead: Boolean,
)

private val sampleNotifications = listOf(
    NotificationDisplay("n1", "\uD83C\uDFE2", "New Internship Match", "Google STEP Internship - 98% match for your profile", "2m ago", NotificationType.OPPORTUNITY_ALERT, false),
    NotificationDisplay("n2", "\u23F0", "Deadline Approaching", "Amazon SDE Internship closes in 3 days", "1h ago", NotificationType.DEADLINE_REMINDER, false),
    NotificationDisplay("n3", "\uD83C\uDFC6", "Achievement Unlocked", "First Streak badge earned! 7-day streak completed.", "3h ago", NotificationType.ACHIEVEMENT_UNLOCKED, true),
    NotificationDisplay("n4", "\uD83D\uDD25", "Streak at Risk", "Complete today's challenge to maintain your 23-day streak!", "5h ago", NotificationType.STREAK_REMINDER, false),
    NotificationDisplay("n5", "\uD83E\uDD16", "AI Insight", "Based on your profile, GSOC opens in 12 days. Your React skills are a strong match!", "1d ago", NotificationType.AI_INSIGHT, true),
    NotificationDisplay("n6", "\uD83D\uDE80", "Level Up!", "You've reached Engineer level! 2,450 XP earned.", "2d ago", NotificationType.LEVEL_UP, true),
    NotificationDisplay("n7", "\uD83D\uDCCA", "Weekly Report", "You applied to 3 internships this week. Career score up by 12 points!", "3d ago", NotificationType.WEEKLY_REPORT, true),
    NotificationDisplay("n8", "\uD83C\uDFAF", "New Hackathon", "Smart India Hackathon registration is now open!", "5d ago", NotificationType.OPPORTUNITY_ALERT, true),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(
    onBack: () -> Unit = {},
) {
    var notifications by remember { mutableStateOf(sampleNotifications) }
    val unreadCount = notifications.count { !it.isRead }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Notifications", fontWeight = FontWeight.Bold)
                    if (unreadCount > 0) {
                        Spacer(Modifier.width(8.dp))
                        Box(
                            modifier = Modifier.size(20.dp).clip(CircleShape)
                                .background(NexusGradients.Error[0]),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text("$unreadCount",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            },
            actions = {
                if (notifications.any { !it.isRead }) {
                    TextButton(onClick = {
                        notifications = notifications.map { it.copy(isRead = true) }
                    }) {
                        Text("Mark all read", style = MaterialTheme.typography.labelMedium)
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
        )

        if (notifications.isEmpty()) {
            EmptyState(
                icon = { Icon(Icons.Default.Notifications, contentDescription = null,
                    modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant) },
                title = "No notifications yet",
                description = "We'll notify you about new opportunities, deadlines, and achievements.",
            )
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(notifications) { notification ->
                    NotificationCard(
                        notification = notification,
                        onRead = {
                            notifications = notifications.map {
                                if (it.id == notification.id) it.copy(isRead = true) else it
                            }
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun NotificationCard(
    notification: NotificationDisplay,
    onRead: () -> Unit,
) {
    AnimatedVisibility(
        visible = true,
        enter = fadeIn() + slideInVertically(animationSpec = spring()) { it / 2 },
    ) {
        NexusCard(
            onClick = if (!notification.isRead) onRead else null,
            modifier = Modifier.fillMaxWidth(),
            shape = NexusShapes.sm,
            glassEffect = notification.isRead,
        ) {
            Row(verticalAlignment = Alignment.Top) {
                Box(
                    modifier = Modifier.size(40.dp).clip(CircleShape)
                        .background(
                            if (notification.isRead) MaterialTheme.colorScheme.surfaceVariant
                            else when (notification.type) {
                                NotificationType.OPPORTUNITY_ALERT -> NexusColors.NexusBlue.copy(alpha = 0.15f)
                                NotificationType.DEADLINE_REMINDER -> NexusColors.PlasmaOrange.copy(alpha = 0.15f)
                                NotificationType.ACHIEVEMENT_UNLOCKED -> NexusColors.StellarPink.copy(alpha = 0.15f)
                                NotificationType.STREAK_REMINDER -> NexusColors.AuroraGreen.copy(alpha = 0.15f)
                                NotificationType.AI_INSIGHT -> NexusColors.ElectricIndigo.copy(alpha = 0.15f)
                                else -> MaterialTheme.colorScheme.surfaceVariant
                            }
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(notification.emoji, style = MaterialTheme.typography.titleSmall)
                }
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            notification.title,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (notification.isRead) FontWeight.Normal
                                else FontWeight.SemiBold,
                            modifier = Modifier.weight(1f),
                        )
                        if (!notification.isRead) {
                            Box(
                                modifier = Modifier.size(8.dp).clip(CircleShape)
                                    .background(NexusColors.NexusBlue)
                            )
                        }
                    }
                    Spacer(Modifier.height(2.dp))
                    Text(
                        notification.body,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        notification.time,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    )
                }
            }
        }
    }
}
