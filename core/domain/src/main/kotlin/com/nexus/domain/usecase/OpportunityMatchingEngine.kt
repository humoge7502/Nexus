package com.nexus.domain.usecase

import com.nexus.domain.model.Internship
import com.nexus.domain.model.Skill
import javax.inject.Inject

class OpportunityMatchingEngine @Inject constructor() {
    fun calculateMatchScore(internship: Internship, userSkills: List<Skill>): Float {
        if (userSkills.isEmpty()) return 0.5f
        val requiredSkillNames = internship.requiredSkills.map { it.name.lowercase() }.toSet()
        if (requiredSkillNames.isEmpty()) return 0.5f
        val userSkillNames = userSkills.map { it.name.lowercase() }.toSet()
        val matched = requiredSkillNames.intersect(userSkillNames).size
        val score = matched.toFloat() / requiredSkillNames.size
        return score.coerceIn(0f, 1f)
    }

    fun rankOpportunities(internships: List<Internship>, userSkills: List<Skill>): List<Internship> {
        return internships.map { it.copy(relevanceScore = calculateMatchScore(it, userSkills)) }
            .sortedByDescending { it.relevanceScore }
    }
}
