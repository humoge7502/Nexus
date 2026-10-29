package com.nexus.data.repository

import com.nexus.common.ApiResult
import com.nexus.domain.repository.ILeetCodeRepository
import com.nexus.domain.repository.LeetCodeStats
import com.nexus.network.service.LeetCodeFunctionsService
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LeetCodeRepository @Inject constructor(
    private val functions: LeetCodeFunctionsService,
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
        val payload = functions.getStats(username).getOrElse { error ->
            return ApiResult.Error(error.message ?: "Network error", code = -1, throwable = error)
        }
        val stats = LeetCodeStats(
            totalSolved = payload.totalSolved,
            easySolved = payload.easySolved,
            mediumSolved = payload.mediumSolved,
            hardSolved = payload.hardSolved,
            ranking = payload.ranking,
        )
        // The server caches too; this layer keeps tab switches off the network.
        statsCache[cacheKey] = CacheEntry(stats, now + CACHE_TTL_MS)
        return ApiResult.Success(stats)
    }

    private companion object {
        const val CACHE_TTL_MS = 10 * 60 * 1000L
    }
}
