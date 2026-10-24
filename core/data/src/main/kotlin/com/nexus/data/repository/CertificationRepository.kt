package com.nexus.data.repository

import com.nexus.common.ApiResult
import com.nexus.database.dao.CertificationDao
import com.nexus.database.entity.CertificationEntity
import com.nexus.domain.model.*
import com.nexus.network.api.NexusApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.collections.immutable.toImmutableList
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CertificationRepository @Inject constructor(
    private val api: NexusApiService,
    private val dao: CertificationDao,
) {
    suspend fun getCertificationCount(): Int = runCatching { dao.getAll().first().size }.getOrDefault(0)

    fun getAllCertifications(): Flow<List<Certification>> =
        dao.getAll().map { entities -> entities.map { it.toDomain() } }

    suspend fun refreshCertifications(): ApiResult<List<Certification>> {
        return try {
            val response = api.getRecommendedCertifications()
            if (response.isSuccessful) {
                val dtos = response.body() ?: emptyList()
                val entities = dtos.map { it.toEntity() }
                dao.insertAll(entities)
                ApiResult.Success(entities.map { it.toDomain() })
            } else {
                ApiResult.Error("Server error: ${response.code()}")
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Network error")
        }
    }

    private fun com.nexus.network.dto.CertificationDto.toEntity() = CertificationEntity(
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
            .map { Skill(name = it.trim()) }.toImmutableList() else kotlinx.collections.immutable.persistentListOf(),
        rating = rating, enrollmentUrl = enrollmentUrl,
        isTrending = isTrending, employerRecognition = employerRecognition,
        price = Price.Free,
    )
}
