package com.nexus.domain.usecase

import com.nexus.domain.model.NexusScore
import com.nexus.domain.model.DsaStatus
import com.nexus.domain.model.Achievement
import com.nexus.domain.model.ScoreComponent
import kotlin.math.roundToInt
import javax.inject.Inject

/** Points each input is worth, out of 1000. Declared once so the ledger a screen draws
 *  cannot drift from the arithmetic that produced the total. */
private const val SKILL_MAX = 250
private const val ENGAGEMENT_MAX = 200
private const val CONSISTENCY_MAX = 200
private const val DSA_MAX = 150
private const val ACHIEVEMENT_MAX = 100
private const val GOAL_MAX = 100

class CareerScoreCalculator @Inject constructor() {
    fun calculate(
        skillProgress: Float,
        opportunityEngagement: Float,
        streak: Int,
        solvedProblems: Int,
        achievements: List<Achievement>,
        completedGoals: Int,
    ): NexusScore {
        val skillScore = skillProgress.coerceIn(0f, 1f) * SKILL_MAX
        val engagementScore = opportunityEngagement.coerceIn(0f, 1f) * ENGAGEMENT_MAX
        val consistencyScore = (streak.coerceAtMost(365) / 365f) * CONSISTENCY_MAX
        val dsaScore = (solvedProblems.coerceAtMost(300) / 300f) * DSA_MAX
        val achievementScore = (achievements.count { it.isUnlocked }.coerceAtMost(20) / 20f) * ACHIEVEMENT_MAX
        val goalScore = (completedGoals.coerceAtMost(20) / 20f) * GOAL_MAX

        val components = listOf(
            component("skill", skillScore, SKILL_MAX),
            component("engagement", engagementScore, ENGAGEMENT_MAX),
            component("consistency", consistencyScore, CONSISTENCY_MAX),
            component("dsa", dsaScore, DSA_MAX),
            component("achievements", achievementScore, ACHIEVEMENT_MAX),
            component("goals", goalScore, GOAL_MAX),
        )
        // Summing the rounded lines rather than the raw floats keeps the ledger footing
        // exactly against the headline number. Each line is then checkable, which is the
        // whole reason the lines exist.
        val overallScore = components.sumOf { it.earned }
        return NexusScore(
            overallScore = overallScore.coerceIn(0, 1000),
            skillProgress = skillProgress,
            opportunityEngagement = opportunityEngagement,
            consistency = consistencyScore / CONSISTENCY_MAX,
            portfolioStrength = achievementScore / ACHIEVEMENT_MAX,
            communityContribution = 0f,
            percentile = 0,
            weeklyChange = 0,
            components = components,
        )
    }

    private fun component(key: String, points: Float, max: Int): ScoreComponent =
        ScoreComponent(key = key, earned = points.roundToInt().coerceIn(0, max), available = max)
}
