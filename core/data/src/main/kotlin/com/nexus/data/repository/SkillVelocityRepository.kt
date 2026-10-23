package com.nexus.data.repository

import com.nexus.database.dao.SkillVelocitySnapshotDao
import com.nexus.database.entity.SkillVelocitySnapshotEntity
import com.nexus.domain.model.SkillVelocity
import com.nexus.domain.repository.ISkillVelocityRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SkillVelocityRepository @Inject constructor(
  private val dao: SkillVelocitySnapshotDao,
) : ISkillVelocityRepository {
  override fun getByUserId(userId: String): Flow<List<SkillVelocity>> =
    dao.getByUserId(userId).map { it.map { e -> e.toDomain() } }

  override fun getByUserAndSkill(userId: String, skillId: String): Flow<List<SkillVelocity>> =
    dao.getByUserAndSkill(userId, skillId).map { it.map { e -> e.toDomain() } }

  override fun getTopVelocitySkills(userId: String, limit: Int): Flow<List<SkillVelocity>> =
    dao.getTopVelocitySkills(userId, limit).map { it.map { e -> e.toDomain() } }

  override suspend fun insert(snapshot: SkillVelocity) {
    dao.insert(snapshot.toEntity())
  }

  override suspend fun insertAll(snapshots: List<SkillVelocity>) {
    dao.insertAll(snapshots.map { it.toEntity() })
  }

  override suspend fun clearForUser(userId: String) {
    dao.clearForUser(userId)
  }
}

private fun SkillVelocitySnapshotEntity.toDomain(): SkillVelocity = SkillVelocity(
  userId = userId, skillId = skillId, skillName = skillName,
  proficiency = proficiency, previousProficiency = previousProficiency,
  velocityScore = velocityScore, daysSinceStart = daysSinceStart,
  onTrackForTarget = onTrackForTarget, recordedAt = recordedAt,
)
private fun SkillVelocity.toEntity() =
  SkillVelocitySnapshotEntity(
    userId = userId, skillId = skillId, skillName = skillName,
    proficiency = proficiency, previousProficiency = previousProficiency,
    velocityScore = velocityScore, daysSinceStart = daysSinceStart,
    onTrackForTarget = onTrackForTarget, recordedAt = recordedAt,
  )
