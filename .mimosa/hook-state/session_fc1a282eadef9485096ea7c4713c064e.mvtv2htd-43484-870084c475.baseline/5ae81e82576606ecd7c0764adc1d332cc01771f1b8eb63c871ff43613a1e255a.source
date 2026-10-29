package com.nexus.domain.model

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

/** Unified feed item across internships, hackathons, and certifications. */
sealed class Opportunity {
    abstract val id: String
    abstract val title: String
    abstract val tags: ImmutableList<String>

    data class InternshipOpportunity(
        override val id: String,
        override val title: String,
        val company: String,
        val location: String,
        val isRemote: Boolean,
        override val tags: ImmutableList<String> = persistentListOf(),
    ) : Opportunity()

    data class HackathonOpportunity(
        override val id: String,
        override val title: String,
        val organizer: String,
        val prizePool: String,
        override val tags: ImmutableList<String> = persistentListOf(),
    ) : Opportunity()

    data class CertificationOpportunity(
        override val id: String,
        override val title: String,
        val provider: String,
        override val tags: ImmutableList<String> = persistentListOf(),
    ) : Opportunity()
}

data class StreakData(
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val isActive: Boolean = false,
    val lastActivityDate: String = "",
    val freezeCount: Int = 0,
)

enum class InsightCategory { GROWTH, APPLICATION, GOAL, SKILL, CAREER, LEARNING }

enum class InsightPriority { CRITICAL, HIGH, NORMAL, LOW }

data class ProactiveInsight(
    val id: String,
    val category: InsightCategory,
    val priority: InsightPriority,
    val title: String,
    val body: String,
    val actionText: String? = null,
    val generatedAt: Long = System.currentTimeMillis(),
)

data class RagOpportunityChunk(
    val opportunityId: String,
    val opportunityType: String,
    val title: String,
    val snippet: String,
    val similarity: Float,
)

data class RagContext(
    val query: String,
    val relevantOpportunities: ImmutableList<RagOpportunityChunk> = persistentListOf(),
)
