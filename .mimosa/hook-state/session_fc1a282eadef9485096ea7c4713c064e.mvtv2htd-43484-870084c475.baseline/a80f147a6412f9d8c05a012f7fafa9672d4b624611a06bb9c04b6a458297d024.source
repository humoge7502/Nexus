package com.nexus.data.repository

import com.nexus.database.dao.PlacementForecastDao
import com.nexus.database.entity.PlacementForecastEntity
import com.nexus.domain.model.PlacementForecast
import com.nexus.domain.repository.IPlacementForecastRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlacementForecastRepository @Inject constructor(
  private val dao: PlacementForecastDao,
) : IPlacementForecastRepository {
  override fun getForecasts(userId: String): Flow<List<PlacementForecast>> =
    dao.getById(userId).map { it.map { e -> e.toDomain() } }

  override suspend fun getLatestForTier(userId: String, tier: String): PlacementForecast? =
    dao.getLatestForTier(userId, tier)?.toDomain()

  override suspend fun insert(forecast: PlacementForecast) {
    dao.insert(forecast.toEntity())
  }

  override suspend fun insertAll(forecasts: List<PlacementForecast>) {
    dao.insertAll(forecasts.map { it.toEntity() })
  }

  override suspend fun deleteOlderThan(userId: String, before: Long) {
    dao.deleteOlderThan(userId, before)
  }
}

private fun PlacementForecastEntity.toDomain(): PlacementForecast = PlacementForecast(
  id = id, userId = userId, companyTier = companyTier,
  probabilityScore = probabilityScore, determiningFactors = determiningFactors.split(","),
  calculatedAt = calculatedAt,
)
private fun PlacementForecast.toEntity() =
  PlacementForecastEntity(
    id = id.ifBlank { UUID.randomUUID().toString() },
    userId = userId, companyTier = companyTier,
    probabilityScore = probabilityScore,
    determiningFactors = determiningFactors.joinToString(","),
    calculatedAt = calculatedAt,
  )
