package com.nexus.data.repository

import com.nexus.common.ApiResult
import com.nexus.domain.repository.ILeetCodeRepository
import com.nexus.domain.repository.LeetCodeStats
import com.nexus.network.service.LeetCodeDirectService
import com.nexus.network.service.LeetCodeFunctionsService
import com.nexus.network.service.LeetCodeGraphQLRequest
import com.nexus.network.service.LeetCodeStatsPayload
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LeetCodeRepository @Inject constructor(
    private val functions: LeetCodeFunctionsService,
    private val direct: LeetCodeDirectService,
) : ILeetCodeRepository {

    private data class CacheEntry(val stats: LeetCodeStats, val expiresAtMs: Long)

    private val statsCache = ConcurrentHashMap<String, CacheEntry>()

    internal var nowMillis: () -> Long = System::currentTimeMillis

    override suspend fun getStats(username: String): ApiResult<LeetCodeStats> {
        val cacheKey = username.trim().lowercase()
        val now = nowMillis()
        statsCache[cacheKey]?.takeIf { it.expiresAtMs > now }?.let {
            return ApiResult.Success(it.stats)
        }
        // Primary: our Cloud Function (Firestore-cached, keeps APK traffic off
        // leetcode.com). Fallback: LeetCode's public GraphQL, so a student who
        // connects today still sees real stats before the backend deploys.
        val stats = fetchViaFunctions(username).getOrElse { functionsError ->
            fetchViaDirect(username).getOrElse { directError ->
                return ApiResult.Error(
                    directError.message ?: functionsError.message ?: "Network error",
                    code = -1,
                    throwable = directError,
                )
            }
        }
        // The server caches too; this layer keeps tab switches off the network.
        statsCache[cacheKey] = CacheEntry(stats, now + CACHE_TTL_MS)
        return ApiResult.Success(stats)
    }

    private suspend fun fetchViaFunctions(username: String): Result<LeetCodeStats> =
        functions.getStats(username).map { payload -> payload.toStats() }

    private suspend fun fetchViaDirect(username: String): Result<LeetCodeStats> = runCatching {
        val response = direct.query(
            LeetCodeGraphQLRequest(
                query = PUBLIC_PROFILE_QUERY,
                variables = mapOf("username" to username),
            ),
        )
        if (response.hasErrors) {
            error(response.errors?.firstOrNull()?.message ?: "LeetCode rejected the query")
        }
        val user = response.data?.matchedUser ?: error("LeetCode profile not found")
        val counts = user.submitStats?.acSubmissionNum.orEmpty()
        fun countOf(difficulty: String): Int =
            counts.firstOrNull { it.difficulty.equals(difficulty, ignoreCase = true) }?.count ?: 0
        LeetCodeStats(
            totalSolved = countOf("All"),
            easySolved = countOf("Easy"),
            mediumSolved = countOf("Medium"),
            hardSolved = countOf("Hard"),
            ranking = user.profile?.ranking ?: 0,
        )
    }

    private fun LeetCodeStatsPayload.toStats() = LeetCodeStats(
        totalSolved = totalSolved,
        easySolved = easySolved,
        mediumSolved = mediumSolved,
        hardSolved = hardSolved,
        ranking = ranking,
    )

    private companion object {
        const val CACHE_TTL_MS = 10 * 60 * 1000L

        /** Public, unauthenticated query for a user's own solved counts. */
        val PUBLIC_PROFILE_QUERY = """
            query userPublicProfile(${"$"}username: String!) {
              matchedUser(username: ${"$"}username) {
                username
                profile { ranking reputation }
                submitStats: submitStatsGlobal {
                  acSubmissionNum { difficulty count }
                }
              }
            }
        """.trimIndent()
    }
}
