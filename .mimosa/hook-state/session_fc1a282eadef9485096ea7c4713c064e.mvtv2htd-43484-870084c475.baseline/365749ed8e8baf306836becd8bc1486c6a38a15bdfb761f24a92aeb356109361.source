package com.nexus.domain.repository

import com.nexus.common.ApiResult
import com.nexus.domain.model.Internship
import kotlinx.coroutines.flow.Flow

interface IInternshipRepository {
    fun getAllInternships(): Flow<List<Internship>>
    fun getSavedInternships(): Flow<List<Internship>>
    suspend fun toggleSave(internshipId: String, isSaved: Boolean)
    suspend fun isSaved(id: String): Boolean
    suspend fun refreshInternships(): ApiResult<List<Internship>>
    suspend fun getCachedInternshipsPage(page: Int, pageSize: Int): List<Internship>
    suspend fun refreshInternshipsPage(page: Int, pageSize: Int): ApiResult<List<Internship>>
}
