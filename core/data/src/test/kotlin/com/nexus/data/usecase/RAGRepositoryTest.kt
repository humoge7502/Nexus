package com.nexus.data.usecase

import com.nexus.database.dao.OpportunityEmbeddingDao
import com.nexus.database.entity.OpportunityEmbeddingEntity
import com.nexus.domain.model.RagContext
import com.nexus.domain.usecase.VectorSimilarityEngine
import com.nexus.network.service.EmbeddingService
import io.mockk.*
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class RAGRepositoryTest {

  private lateinit var embeddingService: EmbeddingService
  private lateinit var embeddingDao: OpportunityEmbeddingDao
  private lateinit var vectorEngine: VectorSimilarityEngine
  private lateinit var ragRepository: RAGRepository

  @Before
  fun setUp() {
    embeddingService = mockk()
    embeddingDao = mockk(relaxed = true)
    vectorEngine = mockk()
    ragRepository = RAGRepository(embeddingService, embeddingDao, vectorEngine)
  }

  @Test
  fun `buildContext returns empty context when no embeddings`() = runBlocking {
    coEvery { embeddingDao.getAllByModel(any()) } returns emptyList()
    val result = ragRepository.buildContext("software engineer internship")
    assertTrue(result.relevantOpportunities.isEmpty())
  }

  @Test
  fun `indexOpportunity inserts new embedding`() = runBlocking {
    val text = "Google internship software engineer"
    val entity = OpportunityEmbeddingEntity(
      opportunityId = "opp-1",
      opportunityType = "INTERNSHIP",
      embedding = byteArrayOf(1, 2, 3),
      modelVersion = "nexus-hash-v1",
    )
    coEvery { embeddingDao.getByOpportunity("opp-1", "INTERNSHIP") } returns null
    coEvery { embeddingService.computeEmbedding(text) } returns byteArrayOf(4, 5, 6)
    coEvery { embeddingDao.insert(any()) } just runs

    ragRepository.indexOpportunity("opp-1", "INTERNSHIP", text)
    coVerify { embeddingDao.insert(any()) }
  }

  @Test
  fun `indexOpportunity skips if same model version exists`() = runBlocking {
    val existing = OpportunityEmbeddingEntity(
      opportunityId = "opp-1",
      opportunityType = "INTERNSHIP",
      embedding = byteArrayOf(1),
      modelVersion = "nexus-hash-v1",
    )
    coEvery { embeddingDao.getByOpportunity("opp-1", "INTERNSHIP") } returns existing

    ragRepository.indexOpportunity("opp-1", "INTERNSHIP", "test")
    coVerify(exactly = 0) { embeddingDao.insert(any()) }
  }

  @Test
  fun `clearIndex deletes all`() = runBlocking {
    coEvery { embeddingDao.deleteAll() } just runs
    ragRepository.clearIndex()
    coVerify { embeddingDao.deleteAll() }
  }
}
