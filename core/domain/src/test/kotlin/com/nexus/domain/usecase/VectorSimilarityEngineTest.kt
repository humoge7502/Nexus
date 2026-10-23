package com.nexus.domain.usecase

import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

class VectorSimilarityEngineTest {

  private val engine = VectorSimilarityEngine()

  @Test
  fun `cosineSimilarity identical vectors returns 1`() = runTest {
    val v = floatArrayOf(0.5f, 0.3f, -0.1f, 0.8f)
    assertEquals(1.0f, engine.cosineSimilarity(v, v), 0.001f)
  }

  @Test
  fun `cosineSimilarity orthogonal vectors returns 0`() = runTest {
    val a = floatArrayOf(1f, 0f, 0f)
    val b = floatArrayOf(0f, 1f, 0f)
    assertEquals(0f, engine.cosineSimilarity(a, b), 0.001f)
  }

  @Test
  fun `cosineSimilarity opposite vectors returns -1`() = runTest {
    val a = floatArrayOf(1f, 0f)
    val b = floatArrayOf(-1f, 0f)
    assertEquals(-1.0f, engine.cosineSimilarity(a, b), 0.001f)
  }

  @Test(expected = IllegalArgumentException::class)
  fun `cosineSimilarity throws on dimension mismatch`() = runTest {
    engine.cosineSimilarity(floatArrayOf(0.1f, 0.2f), floatArrayOf(0.1f))
  }

  @Test
  fun `topKMatches filters by threshold`() = runTest {
    val query = floatArrayOf(1f, 0f, 0f)
    val candidates = listOf(
      "a" to floatArrayOf(0.9f, 0.1f, 0f),
      "b" to floatArrayOf(0.1f, 0.9f, 0f),
      "c" to floatArrayOf(0.95f, 0.05f, 0f),
    )
    val results = engine.topKMatches(query, candidates, topK = 5, threshold = 0.8f)
    assertTrue(results.any { it.first == "a" })
    assertFalse(results.any { it.first == "b" })
  }

  @Test
  fun `topKMatches respects topK limit`() = runTest {
    val query = floatArrayOf(1f, 0f, 0f)
    val candidates = (0 until 10).map { i ->
      "id$i" to floatArrayOf(0.9f - i * 0.05f, (i * 0.1f), 0f)
    }
    val results = engine.topKMatches(query, candidates, topK = 3, threshold = 0f)
    assertEquals(3, results.size)
  }

  @Test
  fun `TOP_K and SIMILARITY_THRESHOLD constants`() {
    assertEquals(5, VectorSimilarityEngine.TOP_K)
    assertEquals(0.4f, VectorSimilarityEngine.SIMILARITY_THRESHOLD, 0.01f)
  }
}
