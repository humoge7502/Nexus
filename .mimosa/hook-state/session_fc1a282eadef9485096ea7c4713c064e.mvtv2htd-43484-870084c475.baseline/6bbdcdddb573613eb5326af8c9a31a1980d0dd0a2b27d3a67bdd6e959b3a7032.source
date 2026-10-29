package com.nexus.network.api

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
 * The token comes from [AuthTokenProvider]'s in-memory cache so request
 * threads never block on DataStore. On a 401 the provider performs one
 * bounded refresh before the request is retried (capped to avoid loops).
 */
@Singleton
class AuthInterceptor @Inject constructor(
    private val tokenProvider: AuthTokenProvider,
) : Interceptor, Authenticator {

    fun updateCachedToken(token: String?) = tokenProvider.updateCachedToken(token)

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
        val token = tokenProvider.token()
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
        val token = tokenProvider.refreshBlocking() ?: return null
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
