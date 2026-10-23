package com.nexus.domain.repository

import com.nexus.common.ApiResult
import com.nexus.domain.model.Certification
import kotlinx.coroutines.flow.Flow

interface ICertificationRepository {
    fun getAllCertifications(): Flow<List<Certification>>
    suspend fun getCertificationCount(): Int
    suspend fun refreshCertifications(): ApiResult<List<Certification>>
}
