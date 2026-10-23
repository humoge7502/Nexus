package com.nexus.data.repository

import com.nexus.database.dao.InterviewReportDao
import com.nexus.database.entity.InterviewReportEntity
import com.nexus.domain.repository.IInterviewReportRepository
import com.nexus.domain.repository.InterviewReport
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InterviewReportRepository @Inject constructor(
  private val dao: InterviewReportDao,
) : IInterviewReportRepository {

  override suspend fun getReportsForCompany(company: String, limit: Int): List<InterviewReport> =
    dao.getReportsForCompany(company, limit).map { it.toDomain() }

  override suspend fun getCompaniesWithReports(): List<String> = dao.getCompaniesWithReports()

  override suspend fun getSuccessfulTopicsForCompany(company: String): List<String> =
    dao.getSuccessfulTopicsForCompany(company)

  override suspend fun getReportCountForCompany(company: String): Int =
    dao.getReportCountForCompany(company)

  override suspend fun insert(report: InterviewReport) {
    dao.insert(report.toEntity())
  }

  override suspend fun getForApplication(applicationId: String): List<InterviewReport> =
    dao.getReportsForApplication(applicationId).map { it.toDomain() }

  override fun getAllReports(): Flow<List<InterviewReport>> =
    dao.getAllReports().map { entities -> entities.map { e -> e.toDomain() } }

  override suspend fun getTotalCount(): Int = dao.getTotalReportCount()
}

private fun InterviewReportEntity.toDomain(): InterviewReport = InterviewReport(
  id = id, applicationId = applicationId, company = company, role = role,
  roundType = roundType, roundNumber = roundNumber, questions = questions,
  difficulty = difficulty, topics = topics, outcome = outcome,
  tips = tips, isAnonymized = isAnonymized, reportedAt = reportedAt,
)

private fun InterviewReport.toEntity() = InterviewReportEntity(
  id = id.ifBlank { UUID.randomUUID().toString() },
  applicationId = applicationId, company = company, role = role,
  roundType = roundType, roundNumber = roundNumber,
  questions = questions, difficulty = difficulty, topics = topics,
  outcome = outcome, tips = tips, isAnonymized = isAnonymized, reportedAt = reportedAt,
)
