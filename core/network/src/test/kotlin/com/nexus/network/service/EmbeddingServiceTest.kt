package com.nexus.network.service

import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

class EmbeddingServiceTest {

  private val service = EmbeddingService()

  @Test
  fun `computeEmbedding returns correct size`() = runTest {
    val bytes = service.computeEmbedding("test query")
    assertEquals(EmbeddingService.EMBEDDING_DIM * 4, bytes.size)
  }

  @Test
  fun `computeEmbedding is deterministic`() = runTest {
    val a = service.computeEmbedding("kotlin android")
    val b = service.computeEmbedding("kotlin android")
    assertArrayEquals(a, b)
  }

  @Test
  fun `different texts produce different embeddings`() = runTest {
    val a = service.computeEmbedding("kotlin android")
    val b = service.computeEmbedding("python django")
    assertFalse(a.contentEquals(b))
  }

  @Test
  fun `decodeEmbedding round trip`() = runTest {
    val text = "internship opportunity at google"
    val bytes = service.computeEmbedding(text)
    val decoded = service.decodeEmbedding(bytes)
    assertEquals(EmbeddingService.EMBEDDING_DIM, decoded.size)
  }

  @Test
  fun `cosineSimilarity of identical vectors is 1`() = runTest {
    val v = floatArrayOf(0.5f, 0.3f, -0.1f, 0.8f)
    val sim = service.cosineSimilarity(v, v)
    assertEquals(1.0f, sim, 0.001f)
  }

  @Test(expected = IllegalArgumentException::class)
  fun `cosineSimilarity throws on dimension mismatch`() = runTest {
    service.cosineSimilarity(floatArrayOf(0.1f, 0.2f), floatArrayOf(0.1f))
  }

  @Test
  fun `cosineSimilarity orthogonal vectors is 0`() = runTest {
    val a = floatArrayOf(1f, 0f)
    val b = floatArrayOf(0f, 1f)
    assertEquals(0f, service.cosineSimilarity(a, b), 0.001f)
  }

  @Test
  fun `MODEL_VERSION is stable`() {
    assertEquals("nexus-tfidf-v1", EmbeddingService.MODEL_VERSION)
  }

  @Test
  fun `semantically similar texts have higher similarity than unrelated texts`() = runTest {
    val tech1 = service.decodeEmbedding(service.computeEmbedding("kotlin android development internship"))
    val tech2 = service.decodeEmbedding(service.computeEmbedding("android developer job kotlin"))
    val unrelated = service.decodeEmbedding(service.computeEmbedding("cooking recipes and baking bread"))

    val similarScore = service.cosineSimilarity(tech1, tech2)
    val unrelatedScore = service.cosineSimilarity(tech1, unrelated)

    assertTrue("Similar texts ($similarScore) should score higher than unrelated ($unrelatedScore)", similarScore > unrelatedScore)
  }

  @Test
  fun `empty text returns zeroed embedding`() = runTest {
    val bytes = service.computeEmbedding("")
    val floats = service.decodeEmbedding(bytes)
    assertTrue(floats.all { it == 0f })
  }

  @Test
  fun `DSA related texts cluster together`() = runTest {
    val dsa1 = service.decodeEmbedding(service.computeEmbedding("dynamic programming recursion algorithm"))
    val dsa2 = service.decodeEmbedding(service.computeEmbedding("graph tree bfs dfs data structure"))
    val career = service.decodeEmbedding(service.computeEmbedding("resume linkedin job application interview"))

    val dsaSim = service.cosineSimilarity(dsa1, dsa2)
    val crossSim = service.cosineSimilarity(dsa1, career)

    assertTrue("DSA texts ($dsaSim) should be more similar to each other than to career texts ($crossSim)", dsaSim > crossSim)
  }
}
