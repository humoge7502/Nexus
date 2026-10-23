package com.nexus.domain.repository

import com.nexus.common.ApiResult
import com.nexus.domain.model.Application
import com.nexus.domain.model.ApplicationStatus
import kotlinx.coroutines.flow.Flow

data class ApplicationCounts(
  val applied: Int = 0,
  val interviewing: Int = 0,
  val offered: Int = 0,
  val accepted: Int = 0,
  val total: Int = 0,
)

interface IApplicationRepository {
  fun getAllApplications(): Flow<List<Application>>
  fun getByStatus(status: ApplicationStatus): Flow<List<Application>>
  fun getApplicationCounts(): Flow<ApplicationCounts>
  suspend fun trackApplication(
    opportunityId: String,
    company: String,
    role: String,
    type: String,
    matchScore: Float,
  )
  suspend fun updateStatus(id: String, status: ApplicationStatus)
  suspend fun delete(id: String)
  suspend fun refreshApplications(): ApiResult<Unit>
}
