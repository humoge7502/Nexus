package com.nexus.common

import timber.log.Timber

object GamificationValidator {
    fun validateXpAward(xpAmount: Int, action: String): Boolean {
        val maxXp = when (action) {
            "login" -> Constants.XP_PER_LOGIN
            "challenge" -> Constants.XP_PER_CHALLENGE
            "application" -> Constants.XP_PER_APPLICATION
            "cert_module" -> Constants.XP_PER_CERT_MODULE
            "certification" -> Constants.XP_PER_CERTIFICATION
            "hackathon_win" -> Constants.XP_PER_HACKATHON_WIN
            "roadmap_milestone" -> Constants.XP_PER_ROADMAP_MILESTONE
            "dsa_easy" -> Constants.XP_PER_DSA_EASY
            "dsa_medium" -> Constants.XP_PER_DSA_MEDIUM
            "dsa_hard" -> Constants.XP_PER_DSA_HARD
            "goal_completed" -> Constants.XP_PER_GOAL_COMPLETED
            "skill_added" -> Constants.XP_PER_SKILL_ADDED
            "resume_analyzed" -> Constants.XP_PER_RESUME_ANALYZED
            "mock_interview" -> Constants.XP_PER_MOCK_INTERVIEW
            "portfolio_project" -> Constants.XP_PER_PORTFOLIO_PROJECT
            "referral" -> Constants.XP_PER_REFERRAL
            else -> return false
        }
        if (xpAmount > maxXp) {
            Timber.w("XP validation failed: $xpAmount exceeds max $maxXp for $action")
            return false
        }
        if (xpAmount <= 0) {
            Timber.w("XP validation failed: $xpAmount is not positive for $action")
            return false
        }
        return true
    }

    fun validateScore(score: Int): Boolean {
        return score in 0..Constants.NEXUS_SCORE_MAX
    }

    fun validateStreakIncrement(currentStreak: Int, newStreak: Int): Boolean {
        if (newStreak < 0) return false
        if (newStreak > currentStreak + 1) return false
        if (newStreak > 365) return false
        return true
    }
}
