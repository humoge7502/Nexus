package com.nexus.data.repository

import com.nexus.common.ApiResult
import com.nexus.database.dao.DSAProblemDao
import com.nexus.database.entity.DSAProblemEntity
import com.nexus.domain.model.DSAProblem
import com.nexus.domain.model.DsaDifficulty
import com.nexus.domain.model.DsaStatus
import com.nexus.network.api.NexusApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DSAProblemRepository @Inject constructor(
    private val api: NexusApiService,
    private val dao: DSAProblemDao,
) {
    fun getSolvedCountFlow(): Flow<Int> = dao.getSolvedCountFlow()

    fun getTotalCount(): Flow<Int> = dao.getTotalCount()

    fun getAllProblems(): Flow<List<DSAProblem>> =
        dao.getAll().map { entities -> entities.map { it.toDomain() } }

    fun getByDifficulty(difficulty: String): Flow<List<DSAProblem>> =
        dao.getByDifficulty(difficulty).map { entities -> entities.map { it.toDomain() } }

    suspend fun getDailyChallenge(): DSAProblem? = dao.getDailyChallenge()?.toDomain()

    suspend fun updateStatus(id: String, status: DsaStatus) {
        dao.updateStatus(id, status.name, System.currentTimeMillis())
        try { api.toggleDSAProblem(id, mapOf("is_solved" to (status == DsaStatus.SOLVED))) } catch (_: Exception) { }
    }

    suspend fun getSolvedCount(): Int = dao.getSolvedCount()

    suspend fun refreshProblems(): ApiResult<List<DSAProblem>> {
        return try {
            val response = api.getDSAProblems()
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

    private fun com.nexus.network.dto.DSAProblemDto.toEntity() = DSAProblemEntity(
        id = id, title = title, difficulty = difficulty, category = topics.firstOrNull() ?: "",
        platform = platform, link = url, topics = topics.joinToString(","),
        frequency = 0.5f,
    )

    private fun DSAProblemEntity.toDomain() = DSAProblem(
        id = id, title = title,
        difficulty = try { DsaDifficulty.valueOf(difficulty) } catch (_: Exception) { DsaDifficulty.MEDIUM },
        category = category, platform = platform, link = link,
        topics = if (topics.isNotBlank()) topics.split(",").map { it.trim() }.toImmutableList() else persistentListOf(),
        frequency = frequency,
        status = try { DsaStatus.valueOf(status) } catch (_: Exception) { DsaStatus.NOT_ATTEMPTED },
        solvedAt = solvedAt?.let { Instant.ofEpochMilli(it) }, userNotes = userNotes,
    )
}
