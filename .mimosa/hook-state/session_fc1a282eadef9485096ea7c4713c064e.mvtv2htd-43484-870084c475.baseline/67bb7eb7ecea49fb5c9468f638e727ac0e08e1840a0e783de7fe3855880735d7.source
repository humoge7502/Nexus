package com.nexus.domain.usecase

import javax.inject.Inject
import kotlin.math.sqrt

class VectorSimilarityEngine @Inject constructor() {

  companion object {
    const val TOP_K = 5
    const val SIMILARITY_THRESHOLD = 0.4f
  }

  fun cosineSimilarity(a: FloatArray, b: FloatArray): Float {
    require(a.size == b.size) { "Dimension mismatch: ${a.size} vs ${b.size}" }
    var dot = 0f
    var normA = 0f
    var normB = 0f
    for (i in a.indices) {
      dot += a[i] * b[i]
      normA += a[i] * a[i]
      normB += b[i] * b[i]
    }
    val denom = sqrt(normA) * sqrt(normB)
    return if (denom > 0.0001f) dot / denom else 0f
  }

  fun topKMatches(
    queryVec: FloatArray,
    candidateVectors: List<Pair<String, FloatArray>>,
    topK: Int = TOP_K,
    threshold: Float = SIMILARITY_THRESHOLD,
  ): List<Pair<String, Float>> {
    if (candidateVectors.isEmpty()) return emptyList()
    return candidateVectors
      .map { (id, vec) -> id to cosineSimilarity(queryVec, vec) }
      .filter { it.second >= threshold }
      .sortedByDescending { it.second }
      .take(topK)
  }

  fun isAboveThreshold(similarity: Float, threshold: Float = SIMILARITY_THRESHOLD): Boolean {
    return similarity >= threshold
  }
}
