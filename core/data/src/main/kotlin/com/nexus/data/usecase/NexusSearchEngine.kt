package com.nexus.data.usecase

import com.nexus.domain.model.Internship
import com.nexus.domain.model.SearchMode
import com.nexus.domain.model.SearchQuery
import com.nexus.domain.model.SearchResult
import com.nexus.domain.model.Skill
import com.nexus.domain.usecase.OpportunityMatchingEngine
import com.nexus.domain.usecase.VectorSimilarityEngine
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NexusSearchEngine @Inject constructor(
    private val similarityEngine: VectorSimilarityEngine,
    private val matchingEngine: OpportunityMatchingEngine,
) {

    suspend fun search(
        query: SearchQuery,
        internships: List<Internship>,
        userSkills: List<Skill>,
    ): List<SearchResult> {
        if (query.queryText.isBlank()) return emptyList()

        val queryVector = getQueryVector(query.queryText)

        return when (query.mode) {
            SearchMode.SEMANTIC -> semanticSearch(queryVector, internships, query.topK, query.minSimilarity)
            SearchMode.KEYWORD -> keywordSearch(query.queryText, internships)
            SearchMode.HYBRID -> {
                val byId = internships.associateBy { it.id }
                val semantic = semanticSearch(queryVector, internships, query.topK * 3, query.minSimilarity * 0.7f)
                    .associateBy { it.id }
                val keyword = keywordSearch(query.queryText, internships).associateBy { it.id }
                val ids = semantic.keys + keyword.keys
                ids.mapNotNull { id ->
                    val internship = byId[id] ?: return@mapNotNull null
                    val semanticScore = semantic[id]?.score ?: 0f
                    val keywordScore = keyword[id]?.score ?: 0f
                    val matchScore = matchingEngine.calculateMatchScore(internship, userSkills)
                        .coerceIn(0f, 1f)
                    SearchResult(
                        id = internship.id,
                        title = internship.title,
                        snippet = "${internship.company.name} - ${internship.location.city}",
                        score = (semanticScore * 0.5f) + (keywordScore * 0.35f) + (matchScore * 0.15f),
                        sourceType = "internship",
                    )
                }.sortedByDescending { it.score }.take(query.topK)
            }
        }
    }

    private fun semanticSearch(
        queryVector: FloatArray,
        internships: List<Internship>,
        topK: Int,
        minSimilarity: Float,
    ): List<SearchResult> {
        val candidates = internships.mapNotNull { internship ->
            val text = "${internship.title} ${internship.company.name} ${internship.tags.joinToString(" ") { it.replace("_", " ") }}"
            val vec = textToVector(text)
            if (vec.isNotEmpty()) {
                val sim = similarityEngine.cosineSimilarity(queryVector, vec)
                if (sim >= minSimilarity) {
                    SearchResult(
                        id = internship.id,
                        title = internship.title,
                        snippet = "${internship.company.name} - ${internship.location.city}",
                        score = sim,
                        sourceType = "internship",
                    )
                } else null
            } else null
        }
        return candidates.sortedByDescending { it.score }.take(topK)
    }

    private fun keywordSearch(
        query: String,
        internships: List<Internship>,
    ): List<SearchResult> {
        val terms = query.lowercase().split("\\s+".toRegex())
        return internships.mapNotNull { internship ->
            val text = "${internship.title} ${internship.company.name} ${internship.tags.joinToString(" ") { it.replace("_", " ") }}".lowercase()
            val matchCount = terms.count { term -> text.contains(term) }
            if (matchCount > 0) {
                SearchResult(
                    id = internship.id,
                    title = internship.title,
                    snippet = "${internship.company.name} - ${internship.location.city}",
                    score = matchCount.toFloat() / terms.size,
                    sourceType = "internship",
                )
            } else null
        }.sortedByDescending { it.score }
    }

    private fun getQueryVector(text: String): FloatArray = textToVector(text)

    /**
     * Sparse bag-of-words embedding. Query and document vectors must use the
     * same feature space; hashing the whole query as a single number produced
     * a vector that was orthogonal to every document vector.
     */
    private fun textToVector(text: String): FloatArray {
        if (text.isBlank()) return floatArrayOf()
        val vec = FloatArray(VECTOR_DIM)
        val words = text.lowercase()
            .split("\\s+".toRegex())
            .filter { it.length > 2 }
        if (words.isEmpty()) return floatArrayOf()
        words.forEach { word ->
            val pos = (word.hashCode() and Int.MAX_VALUE) % VECTOR_DIM
            vec[pos] += 1f
        }
        val norm = kotlin.math.sqrt(vec.sumOf { (it * it).toDouble() }).toFloat()
        if (norm > 0f) for (i in vec.indices) vec[i] /= norm
        return vec
    }

    fun calculateRelevanceScore(internship: Internship, query: String): Float {
        val titleMatch = if (internship.title.contains(query, ignoreCase = true)) 0.5f else 0f
        val companyMatch = if (internship.company.name.contains(query, ignoreCase = true)) 0.3f else 0f
        val tagMatch = if (internship.tags.any { it.contains(query, ignoreCase = true) }) 0.2f else 0f
        return titleMatch + companyMatch + tagMatch
    }

    private companion object {
        const val VECTOR_DIM = 128
    }
}
