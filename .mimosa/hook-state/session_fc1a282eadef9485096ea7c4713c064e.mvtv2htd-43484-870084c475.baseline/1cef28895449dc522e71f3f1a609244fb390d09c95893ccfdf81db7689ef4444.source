package com.nexus.network.service

import com.google.firebase.functions.FirebaseFunctions
import kotlinx.coroutines.tasks.await
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
) {
    suspend fun getStats(username: String): Result<LeetCodeStatsPayload> = runCatching {
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
}

data class LeetCodeStatsPayload(
    val totalSolved: Int,
    val easySolved: Int,
    val mediumSolved: Int,
    val hardSolved: Int,
    val ranking: Int,
    val cached: Boolean = false,
)
