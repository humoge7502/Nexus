package com.nexus.data.repository

import com.nexus.common.ApiResult
import com.nexus.database.dao.DSAProblemDao
import com.nexus.database.entity.DSAProblemEntity
import com.nexus.domain.repository.ILeetCodeRepository
import com.nexus.network.api.LeetCodeApiService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LeetCodeRepository @Inject constructor(
    private val api: LeetCodeApiService,
    private val dao: DSAProblemDao,
) : ILeetCodeRepository {
    override suspend fun getStats(username: String): ApiResult<com.nexus.domain.repository.LeetCodeStats> {
        return try {
            val response = api.getUserStats(username)
            if (response.isSuccessful) {
                val dto = response.body() ?: return ApiResult.Error("Empty response")
                ApiResult.Success(
                    com.nexus.domain.repository.LeetCodeStats(
                        totalSolved = dto.totalSolved,
                        easySolved = dto.easySolved,
                        mediumSolved = dto.mediumSolved,
                        hardSolved = dto.hardSolved,
                        ranking = dto.ranking,
                        contributionPoints = 0,
                    )
                )
            } else ApiResult.Error("Server error: ${response.code()}")
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Network error", code = -1, throwable = e)
        }
    }

suspend fun syncSolvedProblems(username: String): ApiResult<Unit> {
        return try {
            val response = api.getSolvedProblems(username)
    if (response.isSuccessful) {
      val problems = response.body() ?: return ApiResult.Error("Empty response")
      problems.forEach { problem ->
        // LeetCode problems use title as the unique identifier in the local DB
        dao.updateStatus(problem.title, "SOLVED", System.currentTimeMillis())
      }
      ApiResult.Success(Unit)
    } else ApiResult.Error("Server error: ${response.code()}")
  } catch (e: Exception) {
    ApiResult.Error(e.message ?: "Network error", code = -1, throwable = e)
  }
    }
}

data class LeetCodeUserStats(
    val username: String,
    val totalSolved: Int,
    val easySolved: Int,
    val mediumSolved: Int,
    val hardSolved: Int,
    val ranking: Int,
)
