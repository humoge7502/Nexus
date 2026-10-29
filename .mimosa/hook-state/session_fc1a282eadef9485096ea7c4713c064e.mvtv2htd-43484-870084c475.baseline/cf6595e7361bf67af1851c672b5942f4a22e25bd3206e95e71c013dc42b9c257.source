package com.nexus.data.repository

import com.nexus.common.ApiResult
import com.nexus.database.dao.GoalDao
import com.nexus.database.entity.GoalEntity
import com.nexus.domain.model.Goal
import com.nexus.domain.model.GoalCategory
import com.nexus.network.api.NexusApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.collections.immutable.persistentListOf
import java.time.Instant
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GoalRepository @Inject constructor(
    private val api: NexusApiService,
    private val dao: GoalDao,
) {
    fun getCompletedCount(): Flow<Int> = dao.getCompletedCount()

    fun getAllGoals(): Flow<List<Goal>> =
        dao.getAll().map { entities -> entities.map { it.toDomain() } }

    fun getActiveGoals(): Flow<List<Goal>> =
        dao.getActive().map { entities -> entities.map { it.toDomain() } }

    suspend fun createGoal(goal: Goal) {
        dao.insert(goal.toEntity())
        try { api.createGoal(goal.toDto()) } catch (_: Exception) { }
    }

    suspend fun updateProgress(id: String, progress: Float) = dao.updateProgress(id, progress)

    suspend fun completeGoal(id: String) = dao.complete(id)

    suspend fun deleteGoal(id: String) {
        dao.delete(id)
        try { api.deleteGoal(id) } catch (_: Exception) { }
    }

    suspend fun refreshGoals(): ApiResult<List<Goal>> {
        return try {
            val response = api.getGoals()
            if (response.isSuccessful) {
                val dtos = response.body() ?: emptyList()
                val entities = dtos.map { it.toEntity() }
                ApiResult.Success(entities.map { it.toDomain() })
            } else {
                ApiResult.Error("Server error: ${response.code()}")
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Network error")
        }
    }

    private fun com.nexus.network.dto.GoalDto.toEntity() = GoalEntity(
        id = id.ifBlank { "goal_${System.currentTimeMillis()}" },
        title = title, description = description, category = category,
        progress = progress,
        targetDate = if (deadline.isNotBlank()) LocalDate.parse(deadline).toEpochDay() else null,
        isCompleted = isCompleted, createdAt = System.currentTimeMillis(),
    )

    private fun Goal.toEntity() = GoalEntity(
        id = id, title = title, description = description, category = category.name,
        progress = progress, targetDate = targetDate?.toEpochDay(),
        isCompleted = isCompleted, createdAt = createdAt.toEpochMilli(),
    )

    private fun Goal.toDto() = com.nexus.network.dto.GoalDto(
        id = id, title = title, description = description, category = category.name,
        progress = progress, deadline = targetDate?.toString() ?: "",
        isCompleted = isCompleted,
    )

    private fun GoalEntity.toDomain() = Goal(
        id = id, title = title, description = description,
        category = try { GoalCategory.valueOf(category) } catch (_: Exception) { GoalCategory.OTHER },
        progress = progress,
        targetDate = targetDate?.let { LocalDate.ofEpochDay(it) },
        isCompleted = isCompleted, createdAt = Instant.ofEpochMilli(createdAt),
        completedAt = null, milestones = persistentListOf(),
    )
}
