package com.nexus.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.nexus.database.entity.OpportunityEmbeddingEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface OpportunityEmbeddingDao {
  @Query("SELECT * FROM opportunity_embeddings ORDER BY cachedAt DESC")
  fun getAll(): Flow<List<OpportunityEmbeddingEntity>>

  @Query("SELECT * FROM opportunity_embeddings WHERE opportunityId = :opportunityId AND opportunityType = :opportunityType LIMIT 1")
  suspend fun getByOpportunity(
    opportunityId: String,
    opportunityType: String,
  ): OpportunityEmbeddingEntity?

  @Query("SELECT * FROM opportunity_embeddings WHERE modelVersion = :modelVersion ORDER BY cachedAt DESC")
  suspend fun getAllByModel(modelVersion: String): List<OpportunityEmbeddingEntity>

  @Query("SELECT COUNT(*) FROM opportunity_embeddings")
  suspend fun getCount(): Int

  @Query("DELETE FROM opportunity_embeddings WHERE modelVersion != :modelVersion")
  suspend fun deleteStale(modelVersion: String)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insert(embedding: OpportunityEmbeddingEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertAll(embeddings: List<OpportunityEmbeddingEntity>)

  @Query("DELETE FROM opportunity_embeddings WHERE opportunityId = :opportunityId AND opportunityType = :opportunityType")
  suspend fun deleteByOpportunity(
    opportunityId: String,
    opportunityType: String,
  )

  @Query("DELETE FROM opportunity_embeddings")
  suspend fun deleteAll()
}
