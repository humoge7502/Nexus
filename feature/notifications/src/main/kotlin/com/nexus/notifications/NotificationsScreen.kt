package com.nexus.notifications

import androidx.compose.ui.res.stringResource
import com.nexus.feature.notifications.R

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nexus.designsystem.component.NexusTopBar
import com.nexus.designsystem.component.hairline
import com.nexus.designsystem.theme.NexusTypography
import com.nexus.domain.model.UiState

data class NotificationDisplay(
    val id: String,
    val icon: ImageVector,
    val title: String,
    val body: String,
    val time: String,
    val type: com.nexus.domain.model.NotificationType,
    val isRead: Boolean,
)

@Composable
fun NotificationsScreen(
    onBack: () -> Unit = {},
    viewModel: NotificationsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val notifications: List<NotificationDisplay> = when (val s = uiState) {
        is UiState.Success -> s.data.map { it.toDisplay() }
        else -> emptyList()
    }
    val unreadCount = notifications.count { !it.isRead }

    Column(modifier = Modifier.fillMaxSize()) {
        NexusTopBar(
            onBack = onBack,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(stringResource(R.string.notifications_notifications), fontWeight = FontWeight.Bold)
                    if (unreadCount > 0) {
                        Spacer(Modifier.width(8.dp))
                        Box(
                            modifier = Modifier.size(20.dp).clip(CircleShape)
                                .background(MaterialTheme.colorScheme.error),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(stringResource(R.string.notifications_unreadcount, unreadCount),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onError)
                        }
                    }
                }
            },
            actions = {
                if (notifications.any { !it.isRead }) {
                    TextButton(onClick = { viewModel.markAllAsRead() }) {
                        Text(stringResource(R.string.notifications_mark_all_read), style = MaterialTheme.typography.labelMedium)
                    }
                }
            },
        )

        if (notifications.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize().padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Icon(
                    Icons.Default.Notifications, contentDescription = null,
                    modifier = Modifier.size(44.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f),
                )
                Spacer(Modifier.height(14.dp))
                Text(
                    stringResource(R.string.notifications_no_notifications_yet),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    stringResource(R.string.notifications_we_ll_notify_you_about_new),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 24.dp, end = 24.dp, top = 10.dp, bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(notifications, key = { it.id }) { notification ->
                    NotificationCard(
                        notification = notification,
                        onRead = { viewModel.markAsRead(notification.id) },
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
    val unread = !notification.isRead
    val shape = RoundedCornerShape(16.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(
                if (unread) MaterialTheme.colorScheme.primary.copy(alpha = 0.06f)
                else MaterialTheme.colorScheme.surfaceContainerLow,
            )
            .hairline(
                color = MaterialTheme.colorScheme.outlineVariant.copy(
                    alpha = if (unread) 0.9f else 0.55f,
                ),
                cornerRadiusDp = 16f,
            )
            .clickable(onClick = onRead)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Icon(
            imageVector = notification.icon,
            contentDescription = null,
            tint = if (unread) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f)
            },
            modifier = Modifier.padding(top = 2.dp).size(18.dp),
        )
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                notification.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = if (unread) FontWeight.SemiBold else FontWeight.Normal,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Spacer(Modifier.height(3.dp))
            Text(
                notification.body,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                notification.time,
                style = NexusTypography.TelemetryMicro,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            )
        }
        if (unread) {
            Spacer(Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .padding(top = 5.dp)
                    .size(5.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
            )
        }
    }
}

private fun com.nexus.domain.model.NotificationItem.toDisplay() = NotificationDisplay(
    id = id,
    icon = when (type) {
        com.nexus.domain.model.NotificationType.OPPORTUNITY_ALERT -> Icons.Default.RocketLaunch
        com.nexus.domain.model.NotificationType.DEADLINE_REMINDER -> Icons.Default.Schedule
        com.nexus.domain.model.NotificationType.ACHIEVEMENT_UNLOCKED -> Icons.Default.EmojiEvents
        com.nexus.domain.model.NotificationType.STREAK_REMINDER -> Icons.Default.LocalFireDepartment
        com.nexus.domain.model.NotificationType.AI_INSIGHT -> Icons.Default.AutoAwesome
        com.nexus.domain.model.NotificationType.LEVEL_UP -> Icons.Default.TrendingUp
        com.nexus.domain.model.NotificationType.WEEKLY_REPORT -> Icons.Default.BarChart
        com.nexus.domain.model.NotificationType.SYSTEM -> Icons.Default.Settings
    },
    title = title,
    body = body,
    time = formatRelative(createdAt),
    type = type,
    isRead = isRead,
)

private fun formatRelative(time: java.time.Instant): String {
    val mins = java.time.Duration.between(time, java.time.Instant.now()).toMinutes()
    return when {
        mins < 1 -> "just now"
        mins < 60 -> mins.toString() + "m ago"
        mins < 60 * 24 -> (mins / 60).toString() + "h ago"
        else -> (mins / (60 * 24)).toString() + "d ago"
    }
}
