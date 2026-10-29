package com.nexus.domain.repository

import com.nexus.common.ApiResult
import com.nexus.domain.model.Achievement
import kotlinx.coroutines.flow.Flow

interface IAchievementRepository {
    fun getAll(): Flow<List<Achievement>>
    fun getUnlocked(): Flow<List<Achievement>>
    fun getEarnedAchievements(): Flow<List<Achievement>>
    suspend fun unlock(id: String)
    suspend fun refreshAchievements(): ApiResult<List<Achievement>>
}
