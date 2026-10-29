package com.nexus.domain.model

data class SearchQuery(
  val queryText: String,
  val mode: SearchMode = SearchMode.HYBRID,
  val topK: Int = 6,
  val minSimilarity: Float = 0.15f,
)

enum class SearchMode {
  SEMANTIC, KEYWORD, HYBRID
}

data class SearchResult(
  val id: String,
  val title: String,
  val snippet: String,
  val score: Float,
  val sourceType: String,
  val url: String? = null,
)
