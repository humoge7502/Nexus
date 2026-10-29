package com.nexus.data.repository

import com.nexus.common.ApiResult
import com.nexus.database.dao.InternshipDao
import com.nexus.database.entity.InternshipEntity
import com.nexus.domain.model.*
import com.nexus.network.api.NexusApiService
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InternshipRepository @Inject constructor(
    private val api: NexusApiService,
    private val dao: InternshipDao,
) {
    fun getAllInternships(): Flow<List<Internship>> =
        dao.getAllInternships().map { entities -> entities.map { it.toDomain() } }

    fun getSavedInternships(): Flow<List<Internship>> =
        dao.getSavedInternships().map { entities -> entities.map { it.toDomain() } }

    suspend fun toggleSave(internshipId: String, isSaved: Boolean) =
        dao.updateSaveStatus(internshipId, isSaved)

    suspend fun isSaved(internshipId: String): Boolean =
        dao.getSavedInternships().first().any { it.id == internshipId }

    suspend fun getCachedInternshipsPage(page: Int, loadSize: Int): List<Internship> =
        dao.getPage(loadSize, page * loadSize).map { it.toDomain() }

    suspend fun refreshInternshipsPage(page: Int, loadSize: Int): ApiResult<List<Internship>> =
        refreshInternships()

    suspend fun refreshInternships(): ApiResult<List<Internship>> {
        return try {
            val response = api.getInternships()
            if (response.isSuccessful) {
                val dtos = response.body()?.data ?: return ApiResult.Error("Empty response")
                val entities = dtos.map { it.toEntity() }
                dao.deleteAll()
                dao.insertAll(entities)
                ApiResult.Success(entities.map { it.toDomain() })
            } else {
                ApiResult.Error("Server error: ${response.code()}")
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Network error")
        }
    }

    private fun com.nexus.network.dto.InternshipDto.toEntity() = InternshipEntity(
        id = id, title = title,
        companyName = company, companyLogoUrl = companyLogo,
        location = location, isRemote = isRemote,
        stipendAmount = stipend?.filter { it.isDigit() }?.toIntOrNull(),
        stipendCurrency = "INR",
        durationMonths = duration.filter { it.isDigit() }.toIntOrNull() ?: 3,
        skills = skills.joinToString(","),
        deadline = java.time.LocalDate.now().toEpochDay(),
        applyUrl = applyUrl, description = description,
        postedAt = System.currentTimeMillis(),
    )

    private fun InternshipEntity.toDomain() = Internship(
        id = id, title = title,
        company = Company(companyName, companyLogoUrl),
        location = Location(location),
        stipend = stipendAmount?.let { Stipend(it, stipendCurrency) },
        duration = Duration(durationMonths),
        deadline = deadlineDate(),
        applyUrl = applyUrl, description = description,
        isRemote = isRemote, isSaved = isSaved,
        postedAt = java.time.Instant.ofEpochMilli(postedAt),
        tags = skillNames(),
        requiredSkills = skillNames().map { Skill(name = it) }.toImmutableList(),
    )

    private fun InternshipEntity.skillNames() =
        if (skills.isBlank()) persistentListOf()
        else skills.split(",").map { it.trim() }.filter { it.isNotBlank() }.toImmutableList()
}
