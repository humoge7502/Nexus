package com.nexus.domain.repository

import com.nexus.common.ApiResult

data class LeetCodeStats(
    val totalSolved: Int = 0,
    val easySolved: Int = 0,
    val mediumSolved: Int = 0,
    val hardSolved: Int = 0,
    val ranking: Int = 0,
    val contributionPoints: Int = 0,
)

interface ILeetCodeRepository {
    suspend fun getStats(username: String): ApiResult<LeetCodeStats>
}
