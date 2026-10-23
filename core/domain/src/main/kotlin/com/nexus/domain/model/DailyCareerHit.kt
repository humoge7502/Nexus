package com.nexus.domain.model

import com.nexus.common.Constants
import java.time.LocalDate

data class DailyCareerHit(
    val id: String,
    val title: String,
    val tip: String,
    val category: CareerHitCategory,
    val xpReward: Int = Constants.XP_PER_DAILY_HIT,
    val date: LocalDate = LocalDate.now(),
    val isViewed: Boolean = false,
)

enum class CareerHitCategory(val displayName: String, val emoji: String) {
    CAREER_TIP("Career Tip", "\uD83D\uDE80"),
    DSA_TIP("DSA Tip", "\uD83D\uDCDA"),
    INTERVIEW_TIP("Interview Tip", "\uD83C\uDFA4"),
    PRODUCTIVITY_TIP("Productivity Tip", "\u26A1"),
    SKILL_TIP("Skill Tip", "\uD83C\uDF93"),
    NETWORKING_TIP("Networking Tip", "\uD83E\uDD1D"),
    RESUME_TIP("Resume Tip", "\uD83D\uDCDC"),
}
