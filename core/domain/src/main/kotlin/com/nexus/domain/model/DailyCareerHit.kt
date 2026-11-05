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

enum class CareerHitCategory(val displayName: String) {
    CAREER_TIP("Career Tip"),
    DSA_TIP("DSA Tip"),
    INTERVIEW_TIP("Interview Tip"),
    PRODUCTIVITY_TIP("Productivity Tip"),
    SKILL_TIP("Skill Tip"),
    NETWORKING_TIP("Networking Tip"),
    RESUME_TIP("Resume Tip"),
}
