package com.nexus.domain.usecase

import javax.inject.Inject

enum class FeatureGate(val displayName: String) {
BASIC_CHAT("AI Chat"),
DSA_TRACKER("DSA Tracker"),
CAREER_HITS("Daily Career Hits"),
STREAK_TRACKING("Streak"),
OPPORTUNITIES("Opportunities"),
ROADMAP("Roadmap Generator"),
CERTIFICATIONS("Certifications"),
SKILL_GAP("Skill Gap Analysis"),
INTERVIEW_COACH("Interview Coach"),
LINKEDIN_POST("LinkedIn Post Gen"),
LEADERBOARD("Leaderboard"),
ORACLE("NEXUS Oracle"),
ARENA("Arena Compete"),
CAREER_INSIGHTS("Career Insights"),
}

class FeatureGateManager @Inject constructor() {

    fun getUnlockedGates(daysSinceOnboarding: Int): Set<FeatureGate> {
        return FeatureGate.entries.filter { gate ->
            when (gate) {
                FeatureGate.BASIC_CHAT,
                FeatureGate.DSA_TRACKER,
                FeatureGate.CAREER_HITS,
                FeatureGate.STREAK_TRACKING,
                FeatureGate.OPPORTUNITIES -> daysSinceOnboarding >= 1
                FeatureGate.ROADMAP,
                FeatureGate.CERTIFICATIONS,
                FeatureGate.SKILL_GAP -> daysSinceOnboarding >= 7
                FeatureGate.INTERVIEW_COACH,
                FeatureGate.LINKEDIN_POST,
                FeatureGate.LEADERBOARD -> daysSinceOnboarding >= 14
                FeatureGate.ORACLE,
                FeatureGate.ARENA,
                FeatureGate.CAREER_INSIGHTS -> daysSinceOnboarding >= 30
            }
        }.toSet()
    }

    fun isFeatureUnlocked(gate: FeatureGate, daysSinceOnboarding: Int): Boolean {
        return gate in getUnlockedGates(daysSinceOnboarding)
    }
}
