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
                val semantic = semanticSearch(queryVector, internships, query.topK * 2, query.minSimilarity * 0.7f)
                val keyword = keywordSearch(query.queryText, internships)
                val combined = (semantic + keyword).distinctBy { it.id }
                val scored = combined.map { result ->
                    val internship = internships.find { it.id == result.id }
                    val matchBonus = if (internship != null) {
                        matchingEngine.calculateMatchScore(internship, userSkills)
                    } else 0f
                    result.copy(score = result.score + (matchBonus * 0.3f))
                }
                scored.sortedByDescending { it.score }.take(query.topK)
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
                    snippet = "${internship.company.name} - ${internship.location}",
                    score = matchCount.toFloat() / terms.size,
                    sourceType = "internship",
                )
            } else null
        }.sortedByDescending { it.score }
    }

    private fun getQueryVector(text: String): FloatArray {
        val dim = 128
        val vec = FloatArray(dim)
        val hash = text.hashCode()
        for (i in 0 until dim) {
            vec[i] = ((hash * (i + 1)) % 1000) / 1000f
        }
        val norm = kotlin.math.sqrt(vec.sumOf { (it * it).toDouble() }).toFloat()
        if (norm > 0f) for (i in vec.indices) vec[i] /= norm
        return vec
    }

    private fun textToVector(text: String): FloatArray {
        if (text.isBlank()) return floatArrayOf()
        val dim = 128
        val vec = FloatArray(dim)
        val words = text.split("\\s+".toRegex()).filter { it.length > 2 }
        words.forEachIndexed { index, word ->
            val hash = word.hashCode()
            val pos = (hash and Int.MAX_VALUE) % dim
            vec[pos] += 1f / words.size
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
}
