package com.nexus.domain.usecase

import com.nexus.domain.model.NexusScore
import com.nexus.domain.model.DsaStatus
import com.nexus.domain.model.Achievement
import javax.inject.Inject

class CareerScoreCalculator @Inject constructor() {
    fun calculate(
        skillProgress: Float,
        opportunityEngagement: Float,
        streak: Int,
        solvedProblems: Int,
        achievements: List<Achievement>,
        completedGoals: Int,
    ): NexusScore {
        val skillScore = skillProgress * 250f
        val engagementScore = opportunityEngagement * 200f
        val consistencyScore = (streak.coerceAtMost(365) / 365f) * 200f
        val dsaScore = (solvedProblems.coerceAtMost(300) / 300f) * 150f
        val achievementScore = (achievements.count { it.isUnlocked }.coerceAtMost(20) / 20f) * 100f
        val goalScore = (completedGoals.coerceAtMost(20) / 20f) * 100f
        val overallScore = (skillScore + engagementScore + consistencyScore + dsaScore + achievementScore + goalScore).toInt()
        return NexusScore(
            overallScore = overallScore.coerceIn(0, 1000),
            skillProgress = skillProgress,
            opportunityEngagement = opportunityEngagement,
            consistency = consistencyScore / 200f,
            portfolioStrength = achievementScore / 100f,
            communityContribution = 0f,
            percentile = 0,
            weeklyChange = 0,
        )
    }
}
