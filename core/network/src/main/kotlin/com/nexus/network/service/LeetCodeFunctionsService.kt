package com.nexus.network.service

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.functions.FirebaseFunctions
import com.nexus.network.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Reads LeetCode stats through our own Cloud Function.
 *
 * The function does the upstream GraphQL call and caches results in Firestore,
 * so the client never depends on community-hosted mirrors and never talks to
 * leetcode.com directly from a distributed APK.
 */
@Singleton
class LeetCodeFunctionsService @Inject constructor(
    private val functions: FirebaseFunctions,
    private val firebaseAuth: FirebaseAuth,
    private val okHttpClient: OkHttpClient,
) {
    suspend fun getStats(username: String): Result<LeetCodeStatsPayload> = runCatching {
        // Raw-HTTP proxy first (sagitta-ai Worker); the callable remains the
        // fallback for a future Blaze deployment. Without a signed-in user the
        // HTTP path cannot authenticate, so it falls through to the callable /
        // direct-GraphQL fallbacks in the repository.
        val configured = BuildConfig.AI_PROXY_URL.trim().trimEnd('/')
        if (configured.isNotBlank()) {
            val base = if (configured.endsWith("/proxyAI")) {
                configured.removeSuffix("/proxyAI")
            } else {
                configured
            }
            val token = firebaseAuth.currentUser?.getIdToken(false)?.await()?.token
            if (token != null) {
                return@runCatching fetchStatsHttp(username, base, token)
            }
        }
        val result = functions
            .getHttpsCallable("leetcodeStats")
            .call(mapOf("username" to username))
            .await()
        @Suppress("UNCHECKED_CAST")
        val data = result.getData() as? Map<String, Any?>
            ?: error("Unexpected LeetCode stats response")
        LeetCodeStatsPayload(
            totalSolved = (data["totalSolved"] as? Number)?.toInt() ?: 0,
            easySolved = (data["easySolved"] as? Number)?.toInt() ?: 0,
            mediumSolved = (data["mediumSolved"] as? Number)?.toInt() ?: 0,
            hardSolved = (data["hardSolved"] as? Number)?.toInt() ?: 0,
            ranking = (data["ranking"] as? Number)?.toInt() ?: 0,
            cached = data["cached"] == true,
        )
    }
    private suspend fun fetchStatsHttp(
        username: String,
        base: String,
        token: String,
    ): LeetCodeStatsPayload = withContext(Dispatchers.IO) {
        val payload = JSONObject().apply { put("username", username) }
        val request = Request.Builder()
            .url("$base/leetcodeStats")
            .addHeader("Authorization", "Bearer $token")
            .post(payload.toString().toRequestBody("application/json; charset=utf-8".toMediaType()))
            .build()
        okHttpClient.newCall(request).execute().use { response ->
            val bodyText = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                throw IllegalStateException("LeetCode stats failed (${response.code})")
            }
            val data = JSONObject(bodyText)
            LeetCodeStatsPayload(
                totalSolved = data.optInt("totalSolved", 0),
                easySolved = data.optInt("easySolved", 0),
                mediumSolved = data.optInt("mediumSolved", 0),
                hardSolved = data.optInt("hardSolved", 0),
                ranking = data.optInt("ranking", 0),
                cached = data.optBoolean("cached", false),
            )
        }
    }
}

data class LeetCodeStatsPayload(
    val totalSolved: Int,
    val easySolved: Int,
    val mediumSolved: Int,
    val hardSolved: Int,
    val ranking: Int,
    val cached: Boolean = false,
)
