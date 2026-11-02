package com.nexus.internships

import com.nexus.domain.model.Internship

internal val internshipFilters = listOf("Remote", "Paid", "AI/ML", "Web", "Mobile", "Cloud")

private val filterKeywords = mapOf(
    "AI/ML" to listOf("ai", "ml", "machine learning", "deep learning", "data science", "nlp", "computer vision"),
    "Web" to listOf("web", "react", "javascript", "typescript", "frontend", "backend", "node", "html", "css", "rest"),
    "Mobile" to listOf("mobile", "android", "ios", "kotlin", "flutter", "swift", "react native"),
    "Cloud" to listOf("cloud", "aws", "gcp", "azure", "devops", "docker", "kubernetes", "serverless"),
)

internal fun List<Internship>.filterInternships(query: String, filters: Set<String>): List<Internship> {
    val normalized = query.trim().lowercase()
    return filter { internship ->
        internship.matchesSearch(normalized) && filters.all { internship.matchesFilter(it) }
    }
}

private fun Internship.searchableText(): String = buildString {
    append(title)
    append(' ')
    append(company.name)
    append(' ')
    append(description)
    append(' ')
    append(location.city)
    append(' ')
    append(tags.joinToString(" "))
    append(' ')
    append(requiredSkills.joinToString(" ") { it.name })
}.lowercase()

private fun Internship.matchesSearch(normalizedQuery: String): Boolean =
    normalizedQuery.isEmpty() ||
        Regex("(^|[^a-z0-9])${Regex.escape(normalizedQuery)}").containsMatchIn(searchableText())

private fun Internship.matchesFilter(filter: String): Boolean = when (filter) {
    "Remote" -> isRemote || location.city.contains("remote", ignoreCase = true)
    "Paid" -> (stipend?.amount ?: 0) > 0
    else -> filterKeywords[filter]?.any { searchableText().containsKeyword(it) } ?: true
}

private fun String.containsKeyword(keyword: String): Boolean =
    if (keyword.length <= 3) {
        Regex("(^|[^a-z0-9])${Regex.escape(keyword)}([^a-z0-9]|$)").containsMatchIn(this)
    } else {
        contains(keyword)
    }
