package com.nexus.data.repository

import com.nexus.common.ApiResult
import com.nexus.database.dao.InternshipDao
import com.nexus.database.entity.InternshipEntity
import com.nexus.domain.model.*
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InternshipRepository @Inject constructor(
    private val feed: OpportunityFeedRepository,
    private val dao: InternshipDao,
) {
    fun getAllInternships(): Flow<List<Internship>> =
        dao.getAllInternships().map { entities -> entities.map { it.toDomain() } }

    fun getSavedInternships(): Flow<List<Internship>> =
        dao.getSavedInternships().map { entities -> entities.map { it.toDomain() } }

    suspend fun toggleSave(internshipId: String, isSaved: Boolean) =
        dao.updateSaveStatus(internshipId, isSaved)

    suspend fun isSaved(internshipId: String): Boolean =
        internshipId in dao.getSavedIds()

    suspend fun getCachedInternshipsPage(page: Int, loadSize: Int): List<Internship> =
        dao.getPage(loadSize, page * loadSize).map { it.toDomain() }

    suspend fun refreshInternshipsPage(page: Int, loadSize: Int): ApiResult<List<Internship>> =
        refreshInternships()

    /**
     * Publishes the verified feed into Room. A failed refresh keeps the last
     * good cache visible rather than blanking the screen.
     */
    suspend fun refreshInternships(): ApiResult<List<Internship>> {
        val cached = dao.getAllInternships().first().map { it.toDomain() }
        return when (val result = feed.refresh()) {
            is ApiResult.Success -> {
                val fresh = dao.getAllInternships().first().map { it.toDomain() }
                ApiResult.Success(fresh.ifEmpty { cached })
            }
            is ApiResult.Error ->
                if (cached.isNotEmpty()) ApiResult.Success(cached)
                else ApiResult.Error(result.message)
            ApiResult.Loading -> ApiResult.Loading
        }
    }

    private fun InternshipEntity.toDomain() = Internship(
        id = id, title = title,
        company = Company(companyName, companyLogoUrl),
        location = Location(location),
        stipend = stipendAmount?.let { Stipend(it, stipendCurrency) },
        duration = Duration(durationMonths),
        deadline = if (deadline > 0L) deadline.toDeadlineDate() else null,
        applyUrl = applyUrl, description = description,
        isRemote = isRemote, isSaved = isSaved,
        postedAt = Instant.ofEpochMilli(postedAt),
        tags = tagNames(),
        requiredSkills = skillNames().map { Skill(name = it) }.toImmutableList(),
        kind = OpportunityKind.from(kind),
        sourceName = sourceName,
        verifiedAt = verifiedAt?.takeIf { it > 0L }?.let(Instant::ofEpochMilli),
        stipendLabel = stipendLabel,
        durationLabel = durationLabel,
    )

    private fun InternshipEntity.skillNames() =
        if (skills.isBlank()) persistentListOf()
        else skills.split(",").map { it.trim() }.filter { it.isNotBlank() }.toImmutableList()

    private fun InternshipEntity.tagNames() =
        if (tags.isBlank()) persistentListOf()
        else tags.split(",").map { it.trim() }.filter { it.isNotBlank() }.toImmutableList()
}
