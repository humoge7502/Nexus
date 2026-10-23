package com.nexus.data.repository

import com.nexus.database.dao.CohortIntelligenceDao
import com.nexus.database.entity.CohortIntelligenceEntity
import com.nexus.domain.model.CohortEntry
import com.nexus.domain.repository.ICohortRepository
import com.nexus.domain.repository.TierBreakdown
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CohortIntelligenceRepository @Inject constructor(
  private val dao: CohortIntelligenceDao,
) : ICohortRepository {
  override fun getByCollege(collegeId: String): Flow<List<CohortEntry>> =
    dao.getByCollege(collegeId).map { it.map { e -> e.toDomain() } }

  override suspend fun getLatestForCollegeAndTier(collegeId: String, tier: String): CohortEntry? =
    dao.getLatestForCollegeAndTier(collegeId, tier)?.toDomain()

  override suspend fun getCollegePlacementRate(collegeId: String): Float? =
    dao.getCollegePlacementRate(collegeId)

  override suspend fun getCollegeBreakdown(collegeId: String): List<TierBreakdown> =
    dao.getCollegeBreakdown(collegeId).map { it.toDomain() }

  override suspend fun insert(entry: CohortEntry) {
    dao.insert(entry.toEntity())
  }

  override suspend fun insertAll(entries: List<CohortEntry>) {
    dao.insertAll(entries.map { it.toEntity() })
  }

  override suspend fun recordPlacement(collegeId: String, tier: String, semester: String) {
    dao.recordPlacement(collegeId, tier, semester)
  }
}

private fun CohortIntelligenceEntity.toDomain(): CohortEntry = CohortEntry(
  id = id, collegeId = collegeId, semester = semester, companyTier = companyTier,
  totalStudents = totalStudents, placedCount = placedCount,
  avgScore = avgScore, avgStreak = avgStreak,
  topSkills = skills.split(",").filter { it.isNotBlank() }, updatedAt = updatedAt,
)

private fun CohortEntry.toEntity() = CohortIntelligenceEntity(
    id = id.ifBlank { UUID.randomUUID().toString() },
    collegeId = collegeId, semester = semester, companyTier = companyTier,
    totalStudents = totalStudents, placedCount = placedCount,
    avgScore = avgScore, avgStreak = avgStreak,
    skills = topSkills.joinToString(","), updatedAt = System.currentTimeMillis(),
  )

private fun com.nexus.database.dao.CollegeTierBreakdown.toDomain() = TierBreakdown(
  companyTier = companyTier, avgScore = avgScore,
  totalPlaced = totalPlaced, totalStudents = totalStudents,
)
