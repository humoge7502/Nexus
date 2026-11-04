package com.nexus.network.service

import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Codeforces' official open API (codeforces.com/api) — fully public, no
 * key, no OAuth. The API answers every call with a `{status, result}`
 * envelope; `FAILED` carries a comment string instead of a result list.
 */
interface CodeforcesService {

    @GET("user.info")
    suspend fun getUserInfo(
        @Query("handles") handles: String,
        @Query("checkHistoricHandles") checkHistoricHandles: Boolean = false,
    ): CodeforcesResponse<CodeforcesUserDto>

    @GET("user.rating")
    suspend fun getRatingHistory(@Query("handle") handle: String): CodeforcesResponse<CodeforcesRatingChangeDto>

    @GET("user.status")
    suspend fun getRecentSubmissions(
        @Query("handle") handle: String,
        @Query("from") from: Int = 1,
        @Query("count") count: Int = 500,
    ): CodeforcesResponse<CodeforcesSubmissionDto>
}

data class CodeforcesResponse<T>(
    val status: String? = null,
    /** Anonymous comment on FAILED; absent on OK. */
    val comment: String? = null,
    val result: List<T>? = null,
)

data class CodeforcesUserDto(
    val handle: String? = null,
    val rating: Int? = null,
    val maxRating: Int? = null,
    val rank: String? = null,
    val maxRank: String? = null,
    val contribution: Int? = null,
    val organization: String? = null,
    val registrationTimeSeconds: Long? = null,
)

data class CodeforcesRatingChangeDto(
    val contestId: Int? = null,
    val contestName: String? = null,
    val rank: Int? = null,
    val ratingUpdateTimeSeconds: Long? = null,
    val oldRating: Int? = null,
    val newRating: Int? = null,
)

data class CodeforcesSubmissionDto(
    val id: Long? = null,
    val creationTimeSeconds: Long? = null,
    val problem: CodeforcesProblemDto? = null,
    val verdict: String? = null,
)

data class CodeforcesProblemDto(
    val name: String? = null,
    val rating: Int? = null,
    val tags: List<String>? = null,
)
