package com.nexus.notifications

import android.app.PendingIntent
import android.content.Intent
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.nexus.datastore.UserPreferences
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class NexusFCMService : FirebaseMessagingService() {

    @Inject lateinit var userPreferences: UserPreferences
    @Inject lateinit var firebaseAuth: FirebaseAuth
    @Inject lateinit var firestore: FirebaseFirestore

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

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
        scope.launch {
            runCatching { userPreferences.saveFcmToken(token) }
                .onFailure { Timber.w(it, "Unable to store FCM token") }
            persistTokenForCurrentUser(token)
        }
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    private suspend fun persistTokenForCurrentUser(token: String) {
        val uid = firebaseAuth.currentUser?.uid ?: return
        runCatching {
            firestore.collection("users").document(uid)
                .set(mapOf("fcmToken" to token), SetOptions.merge())
        }.onFailure { Timber.w(it, "Unable to persist FCM token") }
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
            "deadline" -> NotificationHelper.CHANNEL_DEADLINES
            "insight" -> NotificationHelper.CHANNEL_AI
            else -> NotificationHelper.CHANNEL_DEFAULT
        }

        NotificationHelper.showNotification(
            this, title, body, channelId, pendingIntent,
        )
    }
}
