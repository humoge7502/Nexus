package com.nexus.data.repository

import com.nexus.common.ApiResult
import com.nexus.database.dao.AchievementDao
import com.nexus.database.entity.AchievementEntity
import com.nexus.domain.model.Achievement
import com.nexus.domain.model.AchievementType
import com.nexus.network.api.NexusApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AchievementRepository @Inject constructor(
    private val api: NexusApiService,
    private val dao: AchievementDao,
) {
    fun getAll(): Flow<List<Achievement>> =
        dao.getAll().map { entities -> entities.map { it.toDomain() } }

    fun getUnlocked(): Flow<List<Achievement>> =
        dao.getUnlocked().map { entities -> entities.map { it.toDomain() } }

    suspend fun unlock(id: String) = dao.unlock(id)

    suspend fun refreshAchievements(): ApiResult<List<Achievement>> {
        return try {
            val response = api.getAchievements()
            if (response.isSuccessful) {
                val dtos = response.body() ?: emptyList()
                val entities = dtos.map { it.toEntity() }
                dao.insertAll(entities)
                ApiResult.Success(entities.map { it.toDomain() })
            } else {
                ApiResult.Error("Server error: ${response.code()}")
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Network error")
        }
    }

    private fun com.nexus.network.dto.AchievementDto.toEntity() = AchievementEntity(
        id = id, type = category, title = title, description = description,
        isUnlocked = unlockedAt != null,
        unlockedAt = unlockedAt?.let { System.currentTimeMillis() },
        progress = progress, maxProgress = 1f, xpReward = (progress * 100).toInt(),
    )

    private fun AchievementEntity.toDomain() = Achievement(
        id = id, title = title, description = description,
        type = try { AchievementType.valueOf(type) } catch (_: Exception) { AchievementType.FIRST_INTERNSHIP },
        isUnlocked = isUnlocked,
        unlockedAt = unlockedAt?.let { Instant.ofEpochMilli(it) },
        progress = progress, maxProgress = maxProgress, xpReward = xpReward,
    )
}
