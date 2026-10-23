package com.nexus.network.api

import com.google.firebase.auth.FirebaseAuth
import com.nexus.datastore.UserPreferences
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Attaches the Firebase ID token ONLY to first-party API hosts. The shared
 * OkHttp client is also used for third-party calls (api.github.com,
 * openrouter.ai, LeetCode mirrors) — leaking the session token there would
 * hand our credential to external servers.
 *
 * Also acts as an OkHttp [Authenticator]: on a 401 it re-reads the stored
 * token once and retries, capped to avoid loops.
 */
@Singleton
class AuthInterceptor @Inject constructor(
    private val userPreferences: UserPreferences,
    @Suppress("unused") private val firebaseAuth: FirebaseAuth,
) : Interceptor, Authenticator {

    @Volatile
    private var cachedToken: String? = null

    fun updateCachedToken(token: String?) {
        cachedToken = token
    }

    private val trustedHosts = setOf(
        "api.nexus.app",
        "api.nexusapp.in",
        "localhost",
        "127.0.0.1",
        "nexus-1a61a.cloudfunctions.net",
        "us-central1-nexus-1a61a.cloudfunctions.net",
    )

    override fun intercept(chain: Interceptor.Chain): Response {
        val host = chain.request().url.host
        if (host !in trustedHosts) {
            return chain.proceed(chain.request())
        }
        // OkHttp interceptors run on dispatcher threads, so a short blocking
        // read of the DataStore value is acceptable here.
        val token = cachedToken ?: runBlocking { userPreferences.authToken.first() }
        if (token != null) cachedToken = token
        val request = if (token.isNullOrBlank()) {
            chain.request()
        } else {
            chain.request().newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        }
        return chain.proceed(request)
    }

    override fun authenticate(route: okhttp3.Route?, response: Response): Request? {
        if (responseCount(response) >= 2) return null
        val token = runBlocking { userPreferences.authToken.first() } ?: return null
        cachedToken = token
        return response.request.newBuilder()
            .header("Authorization", "Bearer $token")
            .build()
    }

    private fun responseCount(response: Response): Int {
        var count = 1
        var prior = response.priorResponse
        while (prior != null) {
            count++
            prior = prior.priorResponse
        }
        return count
    }
}
