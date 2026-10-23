package com.nexus.data.repository

import com.nexus.database.dao.ScoreSnapshotDao
import com.nexus.database.entity.ScoreSnapshotEntity
import com.nexus.domain.model.ScoreSnapshot
import com.nexus.domain.repository.IScoreSnapshotRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScoreSnapshotRepository @Inject constructor(
  private val dao: ScoreSnapshotDao,
) : IScoreSnapshotRepository {
  override fun getHistory(userId: String): Flow<List<ScoreSnapshot>> =
    dao.getHistoryForUser(userId).map { it.map { e -> e.toDomain() } }

  override fun getRecentSnapshots(userId: String, limit: Int): Flow<List<ScoreSnapshot>> =
    dao.getRecentSnapshots(userId, limit).map { it.map { e -> e.toDomain() } }

  override suspend fun getLatest(userId: String): ScoreSnapshot? =
    dao.getLatestSnapshot(userId)?.toDomain()

  override suspend fun insert(snapshot: ScoreSnapshot) {
    dao.insert(snapshot.toEntity())
  }

  override suspend fun deleteOlderThan(userId: String, before: Long) {
    dao.deleteOlderThan(userId, before)
  }
}

private fun ScoreSnapshotEntity.toDomain(): ScoreSnapshot = ScoreSnapshot(
  id = id, userId = userId, score = score, componentsJson = components,
  delta = delta, recordedAt = recordedAt,
)
private fun ScoreSnapshot.toEntity() = ScoreSnapshotEntity(
  id = id.ifBlank { UUID.randomUUID().toString() },
  userId = userId, score = score, components = componentsJson,
  delta = delta, recordedAt = recordedAt,
)
