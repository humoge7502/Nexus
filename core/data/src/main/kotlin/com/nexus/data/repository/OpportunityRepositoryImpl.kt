package com.nexus.data.repository

import com.nexus.common.ApiResult
import com.nexus.common.NetworkMonitor
import com.nexus.database.dao.CertificationDao
import com.nexus.database.dao.HackathonDao
import com.nexus.database.dao.InternshipDao
import com.nexus.database.entity.CertificationEntity
import com.nexus.database.entity.HackathonEntity
import com.nexus.database.entity.InternshipEntity
import com.nexus.domain.model.Certification
import com.nexus.domain.model.CertProvider
import com.nexus.domain.model.Company
import com.nexus.domain.model.DifficultyLevel
import com.nexus.domain.model.Duration
import com.nexus.domain.model.EventMode
import com.nexus.domain.model.Hackathon
import com.nexus.domain.model.Internship
import com.nexus.domain.model.Location
import com.nexus.domain.model.Price
import com.nexus.domain.model.Opportunity
import com.nexus.domain.model.OpportunityKind
import com.nexus.domain.model.Skill
import com.nexus.domain.model.SkillCategory
import com.nexus.domain.model.Stipend
import com.nexus.domain.repository.IOpportunityRepository
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OpportunityRepositoryImpl @Inject constructor(
    private val feed: OpportunityFeedRepository,
    private val internshipDao: InternshipDao,
    private val hackathonDao: HackathonDao,
    private val certificationDao: CertificationDao,
    private val networkMonitor: NetworkMonitor,
) : IOpportunityRepository {
    override fun getHackathons(): Flow<List<Hackathon>> = hackathonDao.getAll().map { it.map { e -> e.toDomain() } }
    override fun getAllInternships(): Flow<List<Internship>> = internshipDao.getAllInternships().map { it.map { e -> e.toDomain() } }
    override fun getSavedInternships(): Flow<List<Internship>> = internshipDao.getSavedInternships().map { it.map { e -> e.toDomain() } }
    override suspend fun toggleSave(internshipId: String, isSaved: Boolean) = internshipDao.updateSaveStatus(internshipId, isSaved)

    override fun getOpportunities(): Flow<List<Opportunity>> = combine(
        internshipDao.getAllInternships(),
        hackathonDao.getAll(),
        certificationDao.getAll(),
    ) { internships, hackathons, certifications ->
        buildList {
            internships.forEach {
                val d = it.toDomain()
                add(Opportunity.InternshipOpportunity(
                    id = d.id, title = d.title, company = d.company.name,
                    location = d.location.city, isRemote = d.isRemote,
                    tags = d.tags
                ))
            }
            hackathons.forEach {
                val d = it.toDomain()
                add(Opportunity.HackathonOpportunity(
                    id = d.id, title = d.title, organizer = d.organizer,
                    prizePool = d.prizePool, tags = d.tags
                ))
            }
            certifications.forEach {
                val d = it.toDomain()
                add(Opportunity.CertificationOpportunity(
                    id = d.id, title = d.title, provider = d.provider.name,
                    tags = d.skills.map { s -> s.name }.toImmutableList()
                ))
            }
        }
    }

    override suspend fun refreshOpportunities(): ApiResult<Unit> {
        val isOnline = networkMonitor.isOnline.first()
        if (!isOnline) return ApiResult.Error("No internet connection")
        return when (val result = feed.refresh()) {
            is ApiResult.Success -> ApiResult.Success(Unit)
            is ApiResult.Error -> ApiResult.Error(result.message)
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

    private fun CertificationEntity.toDomain() = Certification(
        id = id, title = title,
        provider = try { CertProvider.valueOf(provider.uppercase().replace(" ", "_")) }
            catch (_: Exception) { CertProvider.OTHER },
        category = SkillCategory.GENERAL,
        difficulty = DifficultyLevel.INTERMEDIATE,
        estimatedHours = estimatedHours,
        skills = if (skills.isNotBlank()) skills.split(",")
            .map { Skill(name = it.trim()) }.toImmutableList() else persistentListOf(),
        rating = rating, enrollmentUrl = enrollmentUrl,
        isTrending = isTrending, employerRecognition = employerRecognition,
        price = Price.Free,
    )
}

// Removed local Opportunity sealed interface to use domain model

