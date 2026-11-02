package com.nexus.data.repository

import com.nexus.common.ApiResult
import com.nexus.database.dao.HackathonDao
import com.nexus.database.entity.HackathonEntity
import com.nexus.domain.model.EventMode
import com.nexus.domain.model.Hackathon
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import java.time.Instant
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HackathonRepository @Inject constructor(
    private val feed: OpportunityFeedRepository,
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
        val cached = dao.getAll().first().map { it.toDomain() }
        return when (val result = feed.refresh()) {
            is ApiResult.Success -> {
                val fresh = dao.getAll().first().map { it.toDomain() }
                ApiResult.Success(fresh.ifEmpty { cached })
            }
            is ApiResult.Error ->
                if (cached.isNotEmpty()) ApiResult.Success(cached)
                else ApiResult.Error(result.message)
            ApiResult.Loading -> ApiResult.Loading
        }
    }

    private fun HackathonEntity.toDomain() = Hackathon(
        id = id, title = title, organizer = organizer, description = description,
        mode = try { EventMode.valueOf(mode) } catch (_: Exception) { EventMode.ONLINE },
        location = location,
        startDate = LocalDate.ofEpochDay(startDate),
        endDate = LocalDate.ofEpochDay(endDate),
        registrationDeadline = LocalDate.ofEpochDay(registrationDeadline),
        prizePool = prizePool, registrationUrl = registrationUrl,
        isLive = isLive, isSaved = isSaved,
        tags = if (tags.isNotBlank()) tags.split(",").map { it.trim() }.filter { it.isNotBlank() }.toImmutableList()
        else persistentListOf(),
        skills = persistentListOf(),
        sourceName = sourceName,
        verifiedAt = verifiedAt?.takeIf { it > 0L }?.let(Instant::ofEpochMilli),
    )
}
