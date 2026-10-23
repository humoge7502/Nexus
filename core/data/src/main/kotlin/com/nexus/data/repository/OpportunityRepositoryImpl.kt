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
import com.nexus.domain.model.Skill
import com.nexus.domain.model.SkillCategory
import com.nexus.domain.model.Stipend
import com.nexus.domain.repository.IOpportunityRepository
import com.nexus.network.api.NexusApiService
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import timber.log.Timber
import java.time.Instant
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OpportunityRepositoryImpl @Inject constructor(
    private val api: NexusApiService,
    private val internshipDao: InternshipDao,
    private val hackathonDao: HackathonDao,
    private val certificationDao: CertificationDao,
    private val networkMonitor: NetworkMonitor,
) : IOpportunityRepository {
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
                    tags = if (it.skills.isNotBlank()) it.skills.split(",").toImmutableList() else persistentListOf()
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

        var hasError = false

        try {
            val internResponse = api.getInternships()
            if (internResponse.isSuccessful) {
                val dtos = internResponse.body()?.data ?: emptyList()
                val entities = dtos.map { it.toInternshipEntity() }
                internshipDao.deleteAll()
                internshipDao.insertAll(entities)
            } else {
                hasError = true
                Timber.w("Internship refresh failed: ${internResponse.code()}")
            }
        } catch (e: Exception) {
            hasError = true
            Timber.e(e, "Internship refresh error")
        }

        try {
            val hackResponse = api.getHackathons()
            if (hackResponse.isSuccessful) {
                val dtos = hackResponse.body()?.data ?: emptyList()
                val entities = dtos.map { it.toHackathonEntity() }
                hackathonDao.insertAll(entities)
            } else {
                hasError = true
                Timber.w("Hackathon refresh failed: ${hackResponse.code()}")
            }
        } catch (e: Exception) {
            hasError = true
            Timber.e(e, "Hackathon refresh error")
        }

        try {
            val certResponse = api.getRecommendedCertifications()
            if (certResponse.isSuccessful) {
                val dtos = certResponse.body() ?: emptyList()
                val entities = dtos.map { it.toCertificationEntity() }
                certificationDao.insertAll(entities)
            } else {
                hasError = true
                Timber.w("Certification refresh failed: ${certResponse.code()}")
            }
        } catch (e: Exception) {
            hasError = true
            Timber.e(e, "Certification refresh error")
        }

        return if (hasError) ApiResult.Error("Partial sync failure")
        else ApiResult.Success(Unit)
    }

private fun com.nexus.network.dto.InternshipDto.toInternshipEntity() = InternshipEntity(
  id = id,
  title = title,
  companyName = company,
  companyLogoUrl = companyLogo,
  location = location,
  isRemote = isRemote,
  stipendAmount = stipend?.filter { it.isDigit() }?.toIntOrNull(),
  stipendCurrency = "INR",
  durationMonths = duration.filter { it.isDigit() }.toIntOrNull() ?: 3,
  skills = skills.joinToString(","),
  deadline = deadline.ifBlank { LocalDate.now().plusDays(30).toString() }.let { raw ->
    runCatching { LocalDate.parse(raw).toEpochDay() }.getOrDefault(LocalDate.now().plusDays(30).toEpochDay())
  },
  applyUrl = applyUrl,
  description = description,
  postedAt = postedAt.ifBlank { System.currentTimeMillis().toString() }.let { raw ->
    raw.toLongOrNull() ?: System.currentTimeMillis()
  },
)

    private fun InternshipEntity.toDomain() = Internship(
        id = id, title = title,
        company = Company(companyName, companyLogoUrl),
        location = Location(location),
        stipend = stipendAmount?.let { Stipend(it, stipendCurrency) },
        duration = Duration(durationMonths),
        deadline = LocalDate.ofEpochDay(deadline),
        applyUrl = applyUrl, description = description,
        isRemote = isRemote, isSaved = isSaved,
        postedAt = Instant.ofEpochMilli(postedAt),
    )

private fun com.nexus.network.dto.HackathonDto.toHackathonEntity() = HackathonEntity(
  id = id,
  title = title,
  organizer = organizer,
  description = description,
  mode = mode,
  location = location,
  startDate = parseDate(startDate) ?: LocalDate.now().toEpochDay(),
  endDate = parseDate(endDate) ?: LocalDate.now().plusDays(7).toEpochDay(),
  registrationDeadline = parseDate(registrationDeadline) ?: LocalDate.now().plusDays(14).toEpochDay(),
  prizePool = prizePool,
  registrationUrl = registrationUrl,
  isLive = isLive,
  isSaved = isSaved,
  tags = tags.joinToString(","),
)

private fun parseDate(raw: String): Long? = try {
  LocalDate.parse(raw.ifBlank { return null }.trim()).toEpochDay()
} catch (_: Exception) { null }

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

    private fun com.nexus.network.dto.CertificationDto.toCertificationEntity() = CertificationEntity(
        id = id, title = title, provider = provider,
        estimatedHours = estimatedHours,
        skills = skills.joinToString(","),
        rating = rating, enrollmentUrl = enrollmentUrl,
        isTrending = rating >= 4.5f,
        employerRecognition = (rating * 20).toInt(),
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

