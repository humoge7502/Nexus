package com.nexus.notifications

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nexus.designsystem.theme.NexusColors
import com.nexus.designsystem.theme.NexusGradients
import com.nexus.domain.model.UiState

data class NotificationDisplay(
    val id: String,
    val emoji: String,
    val title: String,
    val body: String,
    val time: String,
    val type: com.nexus.domain.model.NotificationType,
    val isRead: Boolean,
)

@OptIn(ExperimentalMaterial3Api::class)
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
                    TextButton(onClick = { viewModel.markAllAsRead() }) {
                        Text("Mark all read", style = MaterialTheme.typography.labelMedium)
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
        )

        if (notifications.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize().padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Icon(
                    Icons.Default.Notifications, contentDescription = null,
                    modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    "No notifications yet",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "We'll notify you about new opportunities, deadlines, and achievements.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
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
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (notification.isRead) Color.Transparent
                else NexusColors.Indigo500.copy(alpha = 0.08f),
                shape = MaterialTheme.shapes.medium,
            )
            .clickable(onClick = onRead)
            .padding(12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Box(
            modifier = Modifier.size(40.dp).clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center,
        ) {
            Text(notification.emoji, fontSize = 18.sp)
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                notification.title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = if (notification.isRead) FontWeight.Normal else FontWeight.SemiBold,
            )
            Spacer(Modifier.height(2.dp))
            Text(
                notification.body,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                notification.time,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            )
        }
        if (!notification.isRead) {
            Spacer(Modifier.width(8.dp))
            Box(
                modifier = Modifier.size(8.dp).clip(CircleShape)
                    .background(NexusColors.Indigo400),
            )
        }
    }
}

private fun com.nexus.domain.model.NotificationItem.toDisplay() = NotificationDisplay(
    id = id,
    emoji = when (type) {
        com.nexus.domain.model.NotificationType.OPPORTUNITY_ALERT -> "🚀"
        com.nexus.domain.model.NotificationType.DEADLINE_REMINDER -> "⏰"
        com.nexus.domain.model.NotificationType.ACHIEVEMENT_UNLOCKED -> "🏆"
        com.nexus.domain.model.NotificationType.STREAK_REMINDER -> "🔥"
        com.nexus.domain.model.NotificationType.AI_INSIGHT -> "🧠"
        com.nexus.domain.model.NotificationType.LEVEL_UP -> "📈"
        com.nexus.domain.model.NotificationType.WEEKLY_REPORT -> "📊"
        com.nexus.domain.model.NotificationType.SYSTEM -> "⚙"
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
