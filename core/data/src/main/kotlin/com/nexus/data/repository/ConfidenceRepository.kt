package com.nexus.data.repository

import com.nexus.database.dao.ConfidenceAssessmentDao
import com.nexus.database.entity.ConfidenceSelfAssessmentEntity
import com.nexus.domain.model.ConfidenceAssessment
import com.nexus.domain.repository.IConfidenceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConfidenceRepository @Inject constructor(
  private val dao: ConfidenceAssessmentDao,
) : IConfidenceRepository {

  override fun getAllAssessments(): Flow<List<ConfidenceAssessment>> =
    dao.getAll().map { it.map { e -> e.toDomain() } }

  override suspend fun getLatestByType(type: String): ConfidenceAssessment? =
    dao.getLatestByType(type)?.toDomain()

  override suspend fun getAvgCalibrationError(): Float = try {
    dao.getAvgCalibrationError()
  } catch (e: Exception) {
    0f
  }

  override suspend fun insert(assessment: ConfidenceAssessment) {
    dao.insert(assessment.toEntity())
  }

  override suspend fun insertAll(assessments: List<ConfidenceAssessment>) {
    dao.insertAll(assessments.map { it.toEntity() })
  }
}

private fun ConfidenceSelfAssessmentEntity.toDomain(): ConfidenceAssessment = ConfidenceAssessment(
  id = id, assessmentType = assessmentType, confidenceScore = confidenceScore,
  actualScore = actualScore, notes = notes, createdAt = createdAt,
)

private fun ConfidenceAssessment.toEntity() = ConfidenceSelfAssessmentEntity(
    id = id.ifBlank { UUID.randomUUID().toString() },
    assessmentType = assessmentType, confidenceScore = confidenceScore,
    actualScore = actualScore, notes = notes, createdAt = createdAt,
  )
