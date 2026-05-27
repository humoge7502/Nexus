package com.nexus.notifications

import android.app.PendingIntent
import android.content.Intent
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class NexusFCMService : FirebaseMessagingService() {

    override fun onMessageReceived(message: RemoteMessage) {
        val title = message.notification?.title
            ?: message.data["title"]
            ?: "NEXUS Update"
        val body = message.notification?.body
            ?: message.data["body"]
            ?: ""

        val type = message.data["type"]
        val targetId = message.data["target_id"]

        showNotification(title, body, type, targetId)
    }

    override fun onNewToken(token: String) {
        // Send token to backend for push notification targeting
        sendTokenToServer(token)
    }

    private fun showNotification(title: String, body: String, type: String?, targetId: String?) {
        val intent = Intent().apply {
            setClassName(packageName, "com.nexus.app.MainActivity")
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            type?.let { putExtra("notification_type", it) }
            targetId?.let { putExtra("target_id", it) }
        }

        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE,
        )

        val channelId = when (type) {
            "opportunity" -> NotificationHelper.CHANNEL_OPPORTUNITIES
            "streak" -> NotificationHelper.CHANNEL_STREAK
            "social" -> NotificationHelper.CHANNEL_SOCIAL
            else -> NotificationHelper.CHANNEL_DEFAULT
        }

        NotificationHelper.showNotification(
            this, title, body, channelId, pendingIntent,
        )
    }

    private fun sendTokenToServer(token: String) {
        // TODO: Call backend API to register FCM token
    }
}
