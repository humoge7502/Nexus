package com.nexus.domain.usecase

import com.nexus.domain.model.Internship
import com.nexus.domain.model.Skill
import javax.inject.Inject

/**
 * Which of a listing's stated requirements the student can already point to.
 * [missing] keeps the posting's own wording and order, because it is rendered to
 * the student as the thing to learn next.
 */
data class SkillFit(
    val required: Int,
    val matched: List<String>,
    val missing: List<String>,
) {
    val ratio: Float get() = if (required == 0) 0f else matched.size.toFloat() / required
}

class OpportunityMatchingEngine @Inject constructor() {

    fun assess(internship: Internship, userSkills: List<Skill>): SkillFit {
        val owned = SkillNames.canonicalSet(userSkills.map { it.name })
        val seen = HashSet<String>(internship.requiredSkills.size)
        val judged = internship.requiredSkills.asSequence()
            .map { it.name.trim() }
            .filter { it.isNotEmpty() }
            .map { it to SkillNames.canonical(it) }
            .filter { (_, canonical) -> canonical.isNotEmpty() && seen.add(canonical) }
            .map { (name, canonical) -> name to (canonical in owned) }
            .toList()
        return SkillFit(
            required = judged.size,
            matched = judged.filter { it.second }.map { it.first },
            missing = judged.filter { !it.second }.map { it.first },
        )
    }

    /**
     * Neutral placeholder when one side of the comparison is missing: a student
     * who named no skills, or a posting that stated none. Callers must not render
     * it as a percentage — see [assess], which reports the same case as zero required.
     */
    fun calculateMatchScore(internship: Internship, userSkills: List<Skill>): Float {
        if (userSkills.isEmpty()) return 0.5f
        val fit = assess(internship, userSkills)
        if (fit.required == 0) return 0.5f
        return fit.ratio.coerceIn(0f, 1f)
    }

    fun rankOpportunities(internships: List<Internship>, userSkills: List<Skill>): List<Internship> {
        return internships.map { it.copy(relevanceScore = calculateMatchScore(it, userSkills)) }
            .sortedByDescending { it.relevanceScore }
    }
}
