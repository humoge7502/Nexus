package com.nexus.data.repository

import com.nexus.common.ApiResult
import com.nexus.domain.repository.CodeforcesProfile
import com.nexus.domain.repository.CodeforcesRatingPoint
import com.nexus.domain.repository.CodeforcesRecentForm
import com.nexus.domain.repository.ICodeforcesRepository
import com.nexus.network.service.CodeforcesResponse
import com.nexus.network.service.CodeforcesService
import retrofit2.HttpException
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CodeforcesRepository @Inject constructor(
    private val api: CodeforcesService,
) : ICodeforcesRepository {

    private data class CacheEntry<T>(val value: T, val expiresAtMs: Long)

    private val profileCache = ConcurrentHashMap<String, CacheEntry<CodeforcesProfile>>()
    private val ratingCache = ConcurrentHashMap<String, CacheEntry<List<CodeforcesRatingPoint>>>()
    private val formCache = ConcurrentHashMap<String, CacheEntry<CodeforcesRecentForm>>()

    internal var nowMillis: () -> Long = System::currentTimeMillis

    override suspend fun getProfile(handle: String): ApiResult<CodeforcesProfile> {
        // Codeforces looks handles up case-insensitively and echoes the
        // canonical casing, so the cache keys lowercase while the API call
        // keeps the casing the student typed.
        val key = normalize(handle)
        val cacheKey = key.lowercase()
        if (key.isBlank()) return ApiResult.Error("Codeforces handle is blank")
        profileCache[cacheKey]?.takeIf { it.expiresAtMs > nowMillis() }?.let {
            return ApiResult.Success(it.value)
        }
        return runCatching {
            val response = api.getUserInfo(handles = key)
            val dto = response.ok().firstOrNull()
                ?: error("Codeforces handle not found: $key")
            CodeforcesProfile(
                handle = dto.handle ?: key,
                rating = dto.rating,
                maxRating = dto.maxRating,
                rank = dto.rank,
                maxRank = dto.maxRank,
                contribution = dto.contribution,
                organization = dto.organization,
            )
        }.fold(
            onSuccess = {
                profileCache[cacheKey] = CacheEntry(it, nowMillis() + CACHE_TTL_MS)
                ApiResult.Success(it)
            },
            onFailure = { e -> ApiResult.Error(apiMessage(e), httpCode(e), e) },
        )
    }

    override suspend fun getRatingHistory(handle: String): ApiResult<List<CodeforcesRatingPoint>> {
        val key = normalize(handle)
        val cacheKey = key.lowercase()
        if (key.isBlank()) return ApiResult.Error("Codeforces handle is blank")
        ratingCache[cacheKey]?.takeIf { it.expiresAtMs > nowMillis() }?.let {
            return ApiResult.Success(it.value)
        }
        return runCatching {
            api.getRatingHistory(handle = key).ok().mapNotNull { dto ->
                val newRating = dto.newRating ?: return@mapNotNull null
                val oldRating = dto.oldRating ?: newRating
                CodeforcesRatingPoint(
                    timeSeconds = dto.ratingUpdateTimeSeconds ?: 0L,
                    contestName = dto.contestName.orEmpty(),
                    oldRating = oldRating,
                    newRating = newRating,
                )
            }
        }.fold(
            onSuccess = {
                ratingCache[cacheKey] = CacheEntry(it, nowMillis() + CACHE_TTL_MS)
                ApiResult.Success(it)
            },
            onFailure = { e -> ApiResult.Error(apiMessage(e), httpCode(e), e) },
        )
    }

    override suspend fun getRecentForm(handle: String): ApiResult<CodeforcesRecentForm> {
        val key = normalize(handle)
        val cacheKey = key.lowercase()
        if (key.isBlank()) return ApiResult.Error("Codeforces handle is blank")
        formCache[cacheKey]?.takeIf { it.expiresAtMs > nowMillis() }?.let {
            return ApiResult.Success(it.value)
        }
        return runCatching {
            val submissions = api.getRecentSubmissions(handle = key).ok()
            val attempts = submissions.size
            val solved = submissions
                .filter { it.verdict.equals("OK", ignoreCase = true) }
                .mapNotNull { it.problem?.name }
                .toHashSet()
                .size
            CodeforcesRecentForm(
                solvedCount = solved,
                attemptsCount = attempts,
                windowSize = attempts,
            )
        }.fold(
            onSuccess = {
                formCache[cacheKey] = CacheEntry(it, nowMillis() + CACHE_TTL_MS)
                ApiResult.Success(it)
            },
            onFailure = { e -> ApiResult.Error(apiMessage(e), httpCode(e), e) },
        )
    }

    private fun <T> CodeforcesResponse<T>.ok(): List<T> {
        if (status.equals("FAILED", ignoreCase = true)) {
            error(comment ?: "Codeforces rejected the request")
        }
        return result.orEmpty()
    }

    private fun normalize(handle: String): String = handle.trim()

    private fun httpCode(e: Throwable): Int = (e as? HttpException)?.code() ?: -1

    private fun apiMessage(e: Throwable): String = when {
        e is HttpException && e.code() == 400 -> "Codeforces rejected the handle"
        e is HttpException -> "Codeforces request failed (${e.code()})"
        else -> e.message ?: "Codeforces request failed"
    }

    private companion object {
        const val CACHE_TTL_MS = 10 * 60 * 1000L
    }
}
