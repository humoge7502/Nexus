package com.nexus.domain.repository

import com.nexus.common.ApiResult
import com.nexus.domain.model.Goal
import kotlinx.coroutines.flow.Flow

interface IGoalRepository {
    fun getAllGoals(): Flow<List<Goal>>
    fun getActiveGoals(): Flow<List<Goal>>
    fun getCompletedCount(): Flow<Int>
    suspend fun createGoal(goal: Goal)
    suspend fun updateProgress(id: String, progress: Float)
    suspend fun completeGoal(id: String)
    suspend fun deleteGoal(id: String)
    suspend fun refreshGoals(): ApiResult<List<Goal>>
}
