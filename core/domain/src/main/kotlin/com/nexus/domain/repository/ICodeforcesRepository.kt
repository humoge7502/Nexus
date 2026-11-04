package com.nexus.domain.repository

import com.nexus.common.ApiResult

data class CodeforcesProfile(
    val handle: String,
    val rating: Int?,
    val maxRating: Int?,
    val rank: String?,
    val maxRank: String?,
    val contribution: Int?,
    val organization: String?,
)

data class CodeforcesRatingPoint(
    /** Epoch seconds of the contest rating update. */
    val timeSeconds: Long,
    val contestName: String,
    val oldRating: Int,
    val newRating: Int,
) {
    val delta: Int get() = newRating - oldRating
}

data class CodeforcesRecentForm(
    /** Unique problems with an accepted verdict inside the inspected window. */
    val solvedCount: Int,
    val attemptsCount: Int,
    /** How many submissions the window actually covered, so the UI labels it honestly. */
    val windowSize: Int,
)

interface ICodeforcesRepository {
    suspend fun getProfile(handle: String): ApiResult<CodeforcesProfile>
    suspend fun getRatingHistory(handle: String): ApiResult<List<CodeforcesRatingPoint>>
    suspend fun getRecentForm(handle: String): ApiResult<CodeforcesRecentForm>
}
