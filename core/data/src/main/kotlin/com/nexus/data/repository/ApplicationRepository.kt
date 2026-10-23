package com.nexus.data.repository

import com.nexus.analytics.AnalyticsTracker
import com.nexus.database.dao.ApplicationDao
import com.nexus.database.dao.CompanyApplicationStats
import com.nexus.database.entity.ApplicationEntity
import com.nexus.domain.model.Application
import com.nexus.domain.model.ApplicationStatus
import com.nexus.common.ApiResult
import com.nexus.domain.repository.IApplicationRepository
import com.nexus.domain.repository.ApplicationCounts
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ApplicationRepository @Inject constructor(
    private val applicationDao: ApplicationDao,
    private val analyticsTracker: AnalyticsTracker,
) : IApplicationRepository {
    override fun getAllApplications(): Flow<List<Application>> =
        applicationDao.getAllApplications().map { entities ->
            entities.map { it.toDomain() }
        }

    override fun getByStatus(status: ApplicationStatus): Flow<List<Application>> =
        applicationDao.getByStatus(status.name).map { entities ->
            entities.map { it.toDomain() }
        }

    override fun getApplicationCounts(): Flow<ApplicationCounts> =
        combine(
            applicationDao.getCountByStatus(ApplicationStatus.APPLIED.name),
            applicationDao.getCountByStatus(ApplicationStatus.INTERVIEWING.name),
            applicationDao.getCountByStatus(ApplicationStatus.OFFERED.name),
            applicationDao.getCountByStatus(ApplicationStatus.ACCEPTED.name),
            applicationDao.getTotalCount(),
        ) { applied, interviewing, offered, accepted, total ->
            ApplicationCounts(applied, interviewing, offered, accepted, total)
        }

    override suspend fun trackApplication(
        opportunityId: String,
        company: String,
        role: String,
        type: String,
        matchScore: Float,
    ) {
        val existing = applicationDao.getByOpportunityId(opportunityId)
        if (existing == null) {
            applicationDao.insert(
                ApplicationEntity(
                    opportunityId = opportunityId,
                    company = company,
                    role = role,
                    opportunityType = type,
                    status = ApplicationStatus.SAVED.name,
                    matchScore = matchScore,
                )
            )
            analyticsTracker.track("application_tracked", mapOf(
                "company" to company, "type" to type, "match_score" to matchScore,
            ))
        }
    }

    override suspend fun updateStatus(id: String, status: ApplicationStatus) {
        val entity = applicationDao.getByOpportunityId(id) ?: return
        val now = System.currentTimeMillis()
        val updated = entity.copy(
            status = status.name,
            appliedDate = if (status == ApplicationStatus.APPLIED && entity.appliedDate == null) now else entity.appliedDate,
            interviewDate = if (status == ApplicationStatus.INTERVIEWING && entity.interviewDate == null) now else entity.interviewDate,
            outcomeDate = if (status in listOf(ApplicationStatus.OFFERED, ApplicationStatus.REJECTED, ApplicationStatus.ACCEPTED)) now else entity.outcomeDate,
            updatedAt = now,
        )
        applicationDao.update(updated)
        analyticsTracker.track("application_status_changed", mapOf(
            "company" to entity.company, "old_status" to entity.status, "new_status" to status.name,
        ))
    }

    fun getCompanyStats(): Flow<List<CompanyApplicationStats>> =
        applicationDao.getCompanyStats()

    override suspend fun delete(id: String) {
        applicationDao.getByOpportunityId(id)?.let { applicationDao.delete(it) }
    }

    override suspend fun refreshApplications(): ApiResult<Unit> {
        return try {
            applicationDao.getAllApplications().first()
            ApiResult.Success(Unit)
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Failed to refresh applications", throwable = e)
        }
    }

    private fun ApplicationEntity.toDomain() = Application(
        id = id, opportunityId = opportunityId, company = company, role = role,
        opportunityType = opportunityType, status = ApplicationStatus.fromString(status),
        matchScore = matchScore, appliedDate = appliedDate, interviewDate = interviewDate,
        interviewRounds = interviewRounds, outcomeDate = outcomeDate, outcomeNotes = outcomeNotes,
        salaryOffered = salaryOffered, preparationNotes = preparationNotes,
        createdAt = createdAt, updatedAt = updatedAt,
    )
}
