package com.nexus.domain.usecase

import java.time.LocalDate

enum class NewMatchKind {
    INTERNSHIP,
    HACKATHON;

    /** The stable prefix used in the announced-key log; matches the wire extra in the alert worker. */
    val keyPrefix: String get() = name
}

/**
 * A feed item the student has never been told about, reduced to what the
 * notification needs. [matchedSkills] is the whole reason the item is a
 * candidate: an opportunity is only worth interrupting for when it asks for
 * something the student can already point to.
 */
data class NewMatchCandidate(
    val kind: NewMatchKind,
    val id: String,
    val title: String,
    val org: String,
    val matchedSkills: List<String>,
    /** Null for rolling admissions — never replaced with an invented date. */
    val deadline: LocalDate?,
) {
    fun key(): String = "${kind.keyPrefix}:$id"
}

/**
 * Decides which of the newly-seen opportunities, if any, becomes a
 * notification. Kept free of Android and Room so the ranking rule can be
 * asserted directly.
 */
object NewMatchPlanner {

    /**
     * The required skills this student already holds, in the order the listing
     * states them. Comparison is trimmed and case-folded because the feed mixes
     * "Node.js", "node" and "NODEJS" across sources, and a missed synonym would
     * silently cost the student an alert.
     */
    fun matchedSkills(required: List<String>, ownedSkills: Set<String>): List<String> {
        if (ownedSkills.isEmpty()) return emptyList()
        val owned = ownedSkills.map { it.trim().lowercase() }.toSet()
        return required.map { it.trim() }
            .filter { it.isNotBlank() && it.lowercase() in owned }
            .distinctBy { it.lowercase() }
    }

    /**
     * One alert per sweep, so the ranking is a decision about urgency rather
     * than a popularity contest: most skills covered first, then the listing
     * that closes soonest, then the title so the choice is reproducible.
     */
    fun select(candidates: List<NewMatchCandidate>, today: LocalDate): NewMatchCandidate? =
        candidates.filter { it.matchedSkills.isNotEmpty() }
            .sortedWith(
                compareByDescending<NewMatchCandidate> { it.matchedSkills.size }
                    .thenBy { it.deadline ?: FAR_FUTURE }
                    .thenBy { it.title.lowercase() }
            )
            .firstOrNull { candidate -> candidate.deadline?.let { !it.isBefore(today) } ?: true }

    private val FAR_FUTURE: LocalDate = LocalDate.of(2100, 1, 1)
}
