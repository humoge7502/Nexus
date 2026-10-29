package com.nexus.explore

import com.nexus.domain.model.Certification
import com.nexus.domain.model.EventMode
import com.nexus.domain.model.Hackathon
import com.nexus.domain.model.Internship
import com.nexus.domain.model.Price

internal const val CATEGORY_INTERNSHIPS = "Internships"
internal const val CATEGORY_HACKATHONS = "Hackathons"
internal const val CATEGORY_CERTIFICATIONS = "Certifications"
internal const val CATEGORY_RESEARCH = "Research"
internal const val CATEGORY_OPEN_SOURCE = "Open Source"
internal const val CATEGORY_SCHOLARSHIPS = "Scholarships"
internal const val CATEGORY_WORKSHOPS = "Workshops"
internal const val CATEGORY_MORE = "More"

internal data class ExploreFilterOption(val label: String, val key: String)

internal val exploreFilterOptions = listOf(
    ExploreFilterOption("Remote", "remote"),
    ExploreFilterOption("Paid", "paid"),
    ExploreFilterOption("AI/ML", "ai/ml"),
    ExploreFilterOption("Web", "web"),
    ExploreFilterOption("Mobile", "mobile"),
    ExploreFilterOption("Cloud", "cloud"),
)

private val categoryKeywords = mapOf(
    CATEGORY_RESEARCH to listOf("research", "science", "phd", "laboratory", "publication"),
    CATEGORY_OPEN_SOURCE to listOf("open source", "opensource", "github", "oss", "maintainer"),
    CATEGORY_SCHOLARSHIPS to listOf("scholarship", "fellowship", "grant", "tuition"),
    CATEGORY_WORKSHOPS to listOf("workshop", "bootcamp", "training", "course", "cohort"),
)

private val tagKeywords = mapOf(
    "ai/ml" to listOf("ai", "ml", "machine learning", "deep learning", "data science", "nlp", "computer vision"),
    "web" to listOf("web", "react", "javascript", "typescript", "frontend", "backend", "node", "html", "css"),
    "mobile" to listOf("mobile", "android", "ios", "kotlin", "flutter", "swift", "react native"),
    "cloud" to listOf("cloud", "aws", "gcp", "azure", "devops", "docker", "kubernetes", "serverless"),
)

internal fun List<Internship>.filterInternshipsForExplore(
    query: String,
    filters: Set<String>,
    category: String,
): List<Internship> {
    if (category == CATEGORY_HACKATHONS || category == CATEGORY_CERTIFICATIONS) return emptyList()
    val normalized = query.trim().lowercase()
    return filter { internship ->
        internship.matchesCategory(category) &&
            internship.matchesSearch(normalized) &&
            filters.all { internship.matchesFilter(it) }
    }
}

internal fun List<Hackathon>.filterHackathonsForExplore(
    query: String,
    filters: Set<String>,
    category: String,
): List<Hackathon> {
    if (category == CATEGORY_CERTIFICATIONS) return emptyList()
    val normalized = query.trim().lowercase()
    return filter { hackathon ->
        hackathon.matchesCategory(category) &&
            hackathon.matchesSearch(normalized) &&
            filters.all { hackathon.matchesFilter(it) }
    }
}

internal fun List<Certification>.filterCertificationsForExplore(
    query: String,
    filters: Set<String>,
    category: String,
): List<Certification> {
    if (category != CATEGORY_CERTIFICATIONS && category != CATEGORY_MORE) return emptyList()
    val normalized = query.trim().lowercase()
    return filter { certification ->
        certification.matchesSearch(normalized) && filters.all { certification.matchesFilter(it) }
    }
}

internal fun Internship.matchesCategory(category: String): Boolean {
    if (category == CATEGORY_INTERNSHIPS || category == CATEGORY_MORE) return true
    val keywords = categoryKeywords[category] ?: return false
    val haystack = searchableText()
    return keywords.any { haystack.containsKeyword(it) }
}

internal fun Hackathon.matchesCategory(category: String): Boolean {
    if (category == CATEGORY_INTERNSHIPS || category == CATEGORY_HACKATHONS || category == CATEGORY_MORE) {
        return true
    }
    val keywords = categoryKeywords[category] ?: return false
    val haystack = searchableText()
    return keywords.any { haystack.containsKeyword(it) }
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

private fun Hackathon.searchableText(): String = buildString {
    append(title)
    append(' ')
    append(organizer)
    append(' ')
    append(description)
    append(' ')
    append(location.orEmpty())
    append(' ')
    append(tags.joinToString(" "))
}.lowercase()

private fun Certification.searchableText(): String = buildString {
    append(title)
    append(' ')
    append(providerName)
    append(' ')
    append(provider.displayName)
    append(' ')
    append(category.name.replace('_', ' '))
    append(' ')
    append(skills.joinToString(" ") { it.name })
}.lowercase()

private fun Internship.matchesSearch(query: String): Boolean =
    query.isEmpty() || searchableText().contains(query)

private fun Hackathon.matchesSearch(query: String): Boolean =
    query.isEmpty() || searchableText().contains(query)

private fun Certification.matchesSearch(query: String): Boolean =
    query.isEmpty() || searchableText().contains(query)

private fun Internship.matchesFilter(filter: String): Boolean = when (filter) {
    "remote" -> isRemote || location.city.contains("remote", ignoreCase = true)
    "paid" -> (stipend?.amount ?: 0) > 0
    else -> tagKeywords[filter]?.any { searchableText().containsKeyword(it) } ?: true
}

private fun Hackathon.matchesFilter(filter: String): Boolean = when (filter) {
    "remote" -> mode == EventMode.ONLINE
    "paid" -> prizePool.any { it.isDigit() }
    else -> tagKeywords[filter]?.any { searchableText().containsKeyword(it) } ?: true
}

private fun Certification.matchesFilter(filter: String): Boolean = when (filter) {
    "remote" -> true
    "paid" -> price is Price.Paid
    else -> tagKeywords[filter]?.any { searchableText().containsKeyword(it) } ?: true
}

private fun String.containsKeyword(keyword: String): Boolean =
    if (keyword.length <= 3) {
        Regex("(^|[^a-z0-9])${Regex.escape(keyword)}([^a-z0-9]|$)").containsMatchIn(this)
    } else {
        contains(keyword)
    }
