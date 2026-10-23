package com.nexus.data.repository

import com.nexus.database.dao.CareerMirrorDao
import com.nexus.database.entity.CareerMirrorEntity
import com.nexus.domain.model.CareerMirror
import com.nexus.domain.repository.ICareerMirrorRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CareerMirrorRepository @Inject constructor(
  private val dao: CareerMirrorDao,
) : ICareerMirrorRepository {
  override fun getByUserId(userId: String): Flow<List<CareerMirror>> =
    dao.getByUserId(userId).map { it.map { e -> e.toDomain() } }

  override suspend fun getLatest(userId: String): CareerMirror? =
    dao.getLatestMirror(userId)?.toDomain()

  override suspend fun insert(mirror: CareerMirror) {
    dao.insert(mirror.toEntity())
  }

  override suspend fun insertAll(mirrors: List<CareerMirror>) {
    dao.insertAll(mirrors.map { it.toEntity() })
  }

  override suspend fun clearForUser(userId: String) {
    dao.clearForUser(userId)
  }
}

private fun CareerMirrorEntity.toDomain(): CareerMirror = CareerMirror(
  id = id, userId = userId, periodStart = periodStart, periodEnd = periodEnd,
  periodLabel = periodLabel, narrative = narrative,
  keyInsights = keyInsights.split("||").filter { it.isNotBlank() },
  actionItems = actionItems.split("||").filter { it.isNotBlank() },
  createdAt = createdAt,
)

private fun CareerMirror.toEntity() = CareerMirrorEntity(
  id = id.ifBlank { UUID.randomUUID().toString() },
  userId = userId, periodStart = periodStart, periodEnd = periodEnd,
  periodLabel = periodLabel, narrative = narrative,
  keyInsights = keyInsights.joinToString("||"),
  actionItems = actionItems.joinToString("||"),
  createdAt = createdAt,
)
