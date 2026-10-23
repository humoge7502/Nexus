package com.nexus.domain.repository

import kotlinx.coroutines.flow.Flow

interface IInterviewReportRepository {
  suspend fun getReportsForCompany(company: String, limit: Int = 20): List<InterviewReport>
  suspend fun getCompaniesWithReports(): List<String>
  suspend fun getSuccessfulTopicsForCompany(company: String): List<String>
  suspend fun getReportCountForCompany(company: String): Int
  suspend fun insert(report: InterviewReport)
  suspend fun getForApplication(applicationId: String): List<InterviewReport>
  fun getAllReports(): Flow<List<InterviewReport>>
  suspend fun getTotalCount(): Int
}

data class InterviewReport(
  val id: String = "",
  val applicationId: String = "",
  val company: String = "",
  val role: String = "",
  val roundType: String = "",
  val roundNumber: Int = 0,
  val questions: String = "",
  val difficulty: String = "",
  val topics: String = "",
  val outcome: String = "",
  val tips: String? = null,
  val isAnonymized: Boolean = true,
  val reportedAt: Long = 0,
)
