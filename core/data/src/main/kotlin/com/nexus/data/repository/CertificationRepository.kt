package com.nexus.data.repository

import com.nexus.common.ApiResult
import com.nexus.database.dao.CertificationDao
import com.nexus.database.dao.CertificationProgressDao
import com.nexus.database.entity.CertificationEntity
import com.nexus.database.entity.CertificationProgressEntity
import com.nexus.domain.model.*
import com.nexus.network.api.NexusApiService
import java.time.Instant
import java.time.LocalDate
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CertificationRepository @Inject constructor(
    private val api: NexusApiService,
    private val dao: CertificationDao,
    private val progressDao: CertificationProgressDao,
) {

    fun getAllCertifications(): Flow<List<Certification>> =
        combine(dao.getAll(), progressDao.getAll()) { entities, progressEntries ->
            val progressById = progressEntries.associateBy { it.certificationId }
            entities.map { it.toDomain(progressById[it.id]) }
        }

    fun getCompletedCertificationCount(): Flow<Int> = progressDao.getCompletedCount()

    suspend fun startCertification(certificationId: String, estimatedHours: Int) {
        if (progressDao.getFor(certificationId) != null) return
        progressDao.upsert(
            CertificationProgressEntity(
                certificationId = certificationId,
                enrolledAt = Instant.now().toEpochMilli(),
                totalModules = moduleCountFor(estimatedHours),
            ),
        )
    }

    /** Returns the updated progress, or null when the certification is not tracked or already complete. */
    suspend fun completeModule(certificationId: String): CertProgress? {
        val current = progressDao.getFor(certificationId) ?: return null
        if (current.completedAt != null || current.completedModules >= current.totalModules) return null
        val next = current.copy(
            completedModules = current.completedModules + 1,
            completedAt = if (current.completedModules + 1 >= current.totalModules) {
                Instant.now().toEpochMilli()
            } else {
                null
            },
        )
        progressDao.upsert(next)
        return next.toDomain()
    }

    suspend fun refreshCertifications(): ApiResult<List<Certification>> {
        return try {
            val response = api.getRecommendedCertifications()
            if (response.isSuccessful) {
                val dtos = response.body() ?: emptyList()
                val entities = dtos.map { it.toEntity() }
                dao.insertAll(entities)
                val progressById = progressDao.getAll().first().associateBy { it.certificationId }
                ApiResult.Success(entities.map { it.toDomain(progressById[it.id]) })
            } else {
                ApiResult.Error("Server error: ${response.code()}")
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Network error")
        }
    }

    private fun moduleCountFor(estimatedHours: Int): Int =
        (estimatedHours / 10).coerceIn(1, 40)

    private fun com.nexus.network.dto.CertificationDto.toEntity() = CertificationEntity(
        id = id, title = title, provider = provider,
        estimatedHours = estimatedHours,
        skills = skills.joinToString(","),
        rating = rating, enrollmentUrl = enrollmentUrl,
        isTrending = rating >= 4.5f,
        employerRecognition = (rating * 20).toInt(),
    )

    private fun CertificationEntity.toDomain(progress: CertificationProgressEntity?) = Certification(
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
        providerName = provider,
        userProgress = progress?.toDomain(),
    )

    private fun CertificationProgressEntity.toDomain() = CertProgress(
        certificationId = certificationId,
        enrolledAt = enrolledAt?.let(Instant::ofEpochMilli),
        targetCompletionDate = targetCompletionDate?.let { runCatching { LocalDate.parse(it) }.getOrNull() },
        completedModules = completedModules,
        totalModules = totalModules,
        studyHoursLogged = studyHoursLogged,
        completedAt = completedAt?.let(Instant::ofEpochMilli),
        certificateUrl = certificateUrl,
    )
}
