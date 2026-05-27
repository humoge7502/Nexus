package com.nexus.data.repository

import com.nexus.common.ApiResult
import com.nexus.database.dao.HackathonDao
import com.nexus.database.entity.HackathonEntity
import com.nexus.domain.model.EventMode
import com.nexus.domain.model.Hackathon
import com.nexus.domain.model.Skill
import com.nexus.network.api.NexusApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HackathonRepository @Inject constructor(
    private val api: NexusApiService,
    private val dao: HackathonDao,
) {
    fun getAllHackathons(): Flow<List<Hackathon>> =
        dao.getAll().map { entities -> entities.map { it.toDomain() } }

    fun getLiveHackathons(): Flow<List<Hackathon>> =
        dao.getLive().map { entities -> entities.map { it.toDomain() } }

    fun getSavedHackathons(): Flow<List<Hackathon>> =
        dao.getSaved().map { entities -> entities.map { it.toDomain() } }

    suspend fun toggleSave(id: String, saved: Boolean) = dao.updateSaveStatus(id, saved)

    suspend fun refreshHackathons(): ApiResult<List<Hackathon>> {
        return try {
            val response = api.getHackathons()
            if (response.isSuccessful) {
                val dtos = response.body()?.data ?: emptyList()
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

    private fun com.nexus.network.dto.HackathonDto.toEntity() = HackathonEntity(
        id = id, title = title, organizer = organizer, description = description,
        mode = mode, location = location,
        startDate = LocalDate.now().toEpochDay(),
        endDate = LocalDate.now().plusDays(2).toEpochDay(),
        registrationDeadline = LocalDate.now().plusDays(3).toEpochDay(),
        prizePool = prizePool, registrationUrl = registrationUrl,
        isLive = isLive, isSaved = isSaved, tags = tags.joinToString(","),
    )

    private fun HackathonEntity.toDomain() = Hackathon(
        id = id, title = title, organizer = organizer, description = description,
        mode = try { EventMode.valueOf(mode) } catch (_: Exception) { EventMode.ONLINE },
        location = location,
        startDate = LocalDate.ofEpochDay(startDate),
        endDate = LocalDate.ofEpochDay(endDate),
        registrationDeadline = LocalDate.ofEpochDay(registrationDeadline),
        prizePool = prizePool, registrationUrl = registrationUrl,
        isLive = isLive, isSaved = isSaved,
        tags = if (tags.isNotBlank()) tags.split(",").toImmutableList() else persistentListOf(),
        skills = persistentListOf(),
    )
}
