package com.nexus.data.usecase

import com.nexus.database.dao.OpportunityEmbeddingDao
import com.nexus.database.entity.OpportunityEmbeddingEntity
import com.nexus.domain.model.RagContext
import com.nexus.domain.model.RagOpportunityChunk
import com.nexus.domain.usecase.VectorSimilarityEngine
import com.nexus.network.service.EmbeddingService
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RAGRepository @Inject constructor(
  private val embeddingService: EmbeddingService,
  private val embeddingDao: OpportunityEmbeddingDao,
  private val vectorSimilarityEngine: VectorSimilarityEngine,
) {

  suspend fun buildContext(query: String, topK: Int = 5): RagContext {
    return try {
      val modelVersion = EmbeddingService.MODEL_VERSION
      val queryVec = embeddingService.decodeEmbedding(embeddingService.computeEmbedding(query))
      val embeddings = embeddingDao.getAllByModel(modelVersion)
      if (embeddings.isEmpty()) return RagContext(query = query)

      val scored = embeddings.map { entity ->
        val vec = embeddingService.decodeEmbedding(entity.embedding)
        val similarity = vectorSimilarityEngine.cosineSimilarity(queryVec, vec)
        entity to similarity
      }.sortedByDescending { it.second }
       .take(topK)

      val chunks = scored.mapNotNull { (entity, similarity) ->
        RagOpportunityChunk(
          opportunityId = entity.opportunityId,
          opportunityType = entity.opportunityType,
          title = entity.opportunityId,
          snippet = "Cached ${entity.opportunityType} opportunity (model: ${entity.modelVersion})",
          similarity = similarity,
        )
      }
      RagContext(
        relevantOpportunities = chunks.toImmutableList(),
        query = query,
      )
    } catch (e: Exception) {
      Timber.w(e, "RAG context build failed")
      RagContext(query = query)
    }
  }

  suspend fun indexOpportunity(opportunityId: String, opportunityType: String, text: String) {
    try {
      val existing = embeddingDao.getByOpportunity(opportunityId, opportunityType)
      val modelVersion = EmbeddingService.MODEL_VERSION
      if (existing != null && existing.modelVersion == modelVersion) return
      val embedding = embeddingService.computeEmbedding(text)
      val entity = OpportunityEmbeddingEntity(
        opportunityId = opportunityId,
        opportunityType = opportunityType,
        embedding = embedding,
        modelVersion = modelVersion,
      )
      embeddingDao.insert(entity)
      Timber.d("Indexed opportunity: $opportunityId ($opportunityType)")
    } catch (e: Exception) {
      Timber.e(e, "Failed to index opportunity: $opportunityId")
    }
  }

  suspend fun indexAll(opportunities: List<Pair<String, String>>) {
    try {
      val modelVersion = EmbeddingService.MODEL_VERSION
      embeddingDao.deleteStale(modelVersion)
      val entities = opportunities.map { (id, type) ->
        val text = "$id $type"
        OpportunityEmbeddingEntity(
          opportunityId = id,
          opportunityType = type,
          embedding = embeddingService.computeEmbedding(text),
          modelVersion = modelVersion,
        )
      }
      embeddingDao.insertAll(entities)
      Timber.d("Batch indexed ${entities.size} opportunities")
    } catch (e: Exception) {
      Timber.e(e, "Batch index failed")
    }
  }

  suspend fun clearIndex() {
    embeddingDao.deleteAll()
  }
}
