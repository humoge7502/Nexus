package com.nexus.common

object Constants {
    const val APP_NAME = "Ascent"
    const val DB_NAME = "nexus.db"
    const val DATASTORE_NAME = "nexus_prefs"

    const val MIN_SDK = 26
    const val COMPILE_SDK = 36
    const val TARGET_SDK = 36

    const val STREAK_GRACE_FREEZES = 2
    const val MAX_RETRY_ATTEMPTS = 5
    const val PAGE_SIZE = 20
    const val INITIAL_PAGE_SIZE = 40
    const val PREFETCH_DISTANCE = 10
    const val MAX_AI_CHAT_FREE_TIER = 10
    const val MIN_SKILL_SELECTION = 2
    const val MAX_SKILL_SELECTION = 5
    const val NEXUS_SCORE_MAX = 1000
    const val XP_PER_LOGIN = 10
    const val XP_ONBOARDING_COMPLETE = 50
    const val XP_PER_DAILY_HIT = 15
    const val XP_PER_CHALLENGE = 25
    const val XP_PER_APPLICATION = 30
    const val XP_PER_CERT_MODULE = 50
    const val XP_PER_CERTIFICATION = 200
    const val XP_PER_HACKATHON_WIN = 500
    const val XP_PER_ROADMAP_MILESTONE = 75
    const val XP_STREAK_BONUS_7 = 100
    const val XP_MAX_PROFILE = 150
    const val XP_PER_DSA_EASY = 10
    const val XP_PER_DSA_MEDIUM = 20
    const val XP_PER_DSA_HARD = 40
    const val XP_PER_GOAL_COMPLETED = 100
    const val XP_PER_SKILL_ADDED = 15
    const val XP_PER_RESUME_ANALYZED = 50
    const val XP_PER_MOCK_INTERVIEW = 80
    const val XP_PER_PORTFOLIO_PROJECT = 120
    const val XP_PER_REFERRAL = 60
    const val XP_PER_ACHIEVEMENT_SHARED = 25
    const val ANIMATION_DURATION_FAST = 300
    const val ANIMATION_DURATION_NORMAL = 600
    const val PLAY_STORE_URL = "https://play.google.com/store/apps/details?id=com.nexus.app"
    const val PRIVACY_POLICY_URL = "https://humoge7502.github.io/Nexus/privacy-policy.html"


    val LEVEL_THRESHOLDS = mapOf(
        "Rookie" to 0..500,
        "Builder" to 501..1500,
        "Engineer" to 1501..4000,
        "Architect" to 4001..10000,
        "Pioneer" to 10001..25000,
        "Ascent Legend" to 25001..Int.MAX_VALUE,
    )

    val BADGE_DEFINITIONS = listOf(
        "First Steps" to "First application, first cert, or first login",
        "Speed Runner" to "Applied within 24hr of opportunity posting",
        "Consistent" to "7, 30, 90, 365 day streaks",
        "Problem Solver" to "50, 150, 300 LeetCode problems",
        "Achiever" to "Landed internship or won hackathon",
        "Portfolio Pro" to "5+ projects showcased",
        "AI Master" to "Used all AI tools",
    )
}
