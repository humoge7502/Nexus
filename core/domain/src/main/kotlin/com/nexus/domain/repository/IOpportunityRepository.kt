package com.nexus.domain.repository

import com.nexus.common.ApiResult
import com.nexus.domain.model.Internship
import com.nexus.domain.model.Opportunity
import kotlinx.coroutines.flow.Flow

interface IOpportunityRepository {
  fun getOpportunities(): Flow<List<Opportunity>>
  suspend fun refreshOpportunities(): ApiResult<Unit>
  fun getAllInternships(): Flow<List<Internship>>
  fun getSavedInternships(): Flow<List<Internship>>
  suspend fun toggleSave(internshipId: String, isSaved: Boolean)
}
