package com.nexus.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

object NotificationHelper {
    const val CHANNEL_DEFAULT = "nexus_default"
    const val CHANNEL_OPPORTUNITIES = "nexus_opportunities"
    const val CHANNEL_STREAK = "nexus_streak"
    const val CHANNEL_SOCIAL = "nexus_social"
    const val CHANNEL_AI = "nexus_ai"
    const val CHANNEL_DEADLINES = "nexus_deadlines"

    private var notificationId = 1000

    fun createChannels(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val channels = listOf(
            NotificationChannel(
                CHANNEL_DEFAULT, "General Updates",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply { description = "General NEXUS notifications" },
            NotificationChannel(
                CHANNEL_OPPORTUNITIES, "Opportunities",
                NotificationManager.IMPORTANCE_HIGH
            ).apply { description = "New internships, hackathons, and opportunities" },
            NotificationChannel(
                CHANNEL_STREAK, "Streaks & Goals",
                NotificationManager.IMPORTANCE_LOW
            ).apply { description = "Streak reminders and goal updates" },
            NotificationChannel(
                CHANNEL_SOCIAL, "Social",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply { description = "Friend activity and community updates" },
            NotificationChannel(
                CHANNEL_AI, "AI Insights",
                NotificationManager.IMPORTANCE_HIGH
            ).apply { description = "Personalized AI career insights" },
        )

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        channels.forEach { manager.createNotificationChannel(it) }
    }

    fun showNotification(
        context: Context,
        title: String,
        body: String,
        channelId: String = CHANNEL_DEFAULT,
        pendingIntent: PendingIntent? = null,
    ) {
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        try {
            NotificationManagerCompat.from(context).notify(notificationId++, notification)
        } catch (_: SecurityException) { }
    }
}
