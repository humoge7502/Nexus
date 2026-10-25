package com.nexus.network.api

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.nexus.datastore.UserPreferences
import com.nexus.network.di.ApplicationScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeoutOrNull
import timber.log.Timber
import java.util.Base64
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Single source of truth for the Firebase ID token used by first-party API
 * calls.
 *
 * OkHttp interceptors are synchronous, so reading DataStore / refreshing a
 * token on every request would block dispatcher threads. This provider keeps
 * an in-memory copy, refreshes it in the background before expiry, and only
 * performs a bounded blocking refresh inside the 401 [okhttp3.Authenticator]
 * path where blocking is mandated by the OkHttp contract.
 */
@Singleton
class AuthTokenProvider @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val userPreferences: UserPreferences,
    @ApplicationScope private val scope: CoroutineScope,
) {

    @Volatile
    private var cachedToken: String? = null

    init {
        firebaseAuth.addAuthStateListener { auth ->
            val user = auth.currentUser
            if (user == null) {
                cachedToken = null
                scope.launch { runCatching { userPreferences.clearAuth() } }
            } else {
                scope.launch { refreshFrom(user, force = false) }
            }
        }
        scope.launch {
            userPreferences.authToken.collect { stored ->
                if (cachedToken.isNullOrBlank() && !stored.isNullOrBlank()) {
                    cachedToken = stored
                }
            }
        }
        // Keeps the cached copy warm while the app is alive.
        scope.launch {
            while (isActive) {
                delay(REFRESH_INTERVAL_MS)
                firebaseAuth.currentUser?.let { refreshFrom(it, force = false) }
            }
        }
    }

    /** Token safe to attach to a request; null when absent or about to expire. */
    fun token(): String? = cachedToken?.takeUnless { JwtExpiry.isExpiring(it, EXPIRY_SKEW_SECONDS) }

    fun updateCachedToken(token: String?) {
        cachedToken = token
    }

    suspend fun refresh(force: Boolean = true): String? {
        val user = firebaseAuth.currentUser ?: run {
            cachedToken = null
            return null
        }
        return refreshFrom(user, force)
    }

    private suspend fun refreshFrom(user: FirebaseUser, force: Boolean): String? {
        return try {
            val token = user.getIdToken(force).await().token
            cachedToken = token
            if (!token.isNullOrBlank()) {
                runCatching { userPreferences.saveAuthToken(token) }
                    .onFailure { Timber.w(it, "Unable to persist refreshed auth token") }
            }
            token
        } catch (e: Exception) {
            Timber.w(e, "Token refresh failed")
            null
        }
    }

    /**
     * Blocking refresh for OkHttp's synchronous Authenticator. Bounded so a
     * stalled network call cannot hold an OkHttp thread indefinitely.
     */
    fun refreshBlocking(timeoutMs: Long = REFRESH_TIMEOUT_MS): String? = runBlocking {
        withTimeoutOrNull(timeoutMs) { refresh(force = true) }
    }

    private companion object {
        const val REFRESH_INTERVAL_MS = 45 * 60 * 1000L
        const val REFRESH_TIMEOUT_MS = 5_000L
        const val EXPIRY_SKEW_SECONDS = 300L
    }
}

internal object JwtExpiry {
    private val expRegex = Regex("\"exp\"\\s*:\\s*(\\d+)")

    fun isExpiring(token: String, skewSeconds: Long): Boolean = runCatching {
        val payload = token.split('.').getOrNull(1) ?: return false
        val json = String(Base64.getUrlDecoder().decode(payload), Charsets.UTF_8)
        val exp = expRegex.find(json)?.groupValues?.getOrNull(1)?.toLongOrNull() ?: return false
        val now = System.currentTimeMillis() / 1000
        exp - now <= skewSeconds
    }.getOrDefault(false)
}
