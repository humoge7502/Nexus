package com.nexus.domain.repository

import com.nexus.common.ApiResult
import com.nexus.domain.model.DSAProblem
import com.nexus.domain.model.DsaDifficulty
import com.nexus.domain.model.DsaStatus
import kotlinx.coroutines.flow.Flow

interface IDSAProblemRepository {
    fun getAllProblems(): Flow<List<DSAProblem>>
    fun getByDifficulty(difficulty: String): Flow<List<DSAProblem>>
    suspend fun getDailyChallenge(): DSAProblem?
    suspend fun updateStatus(id: String, status: DsaStatus)
    suspend fun getSolvedCount(): Int
    suspend fun refreshProblems(): ApiResult<List<DSAProblem>>
}
