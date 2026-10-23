package com.nexus.domain.usecase

import com.nexus.domain.model.AchievementType
import javax.inject.Inject

class BadgeUnlockEvaluator @Inject constructor() {

    data class UserStats(
        val streakDays: Int,
        val problemsSolved: Int,
        val certificationsCompleted: Int,
        val applicationsSubmitted: Int,
        val projectsCompleted: Int,
        val totalXP: Int,
    )

    fun evaluate(stats: UserStats, currentUnlocked: Set<AchievementType>): List<AchievementType> {
        val toUnlock = mutableListOf<AchievementType>()

        if (AchievementType.STREAK_7 !in currentUnlocked && stats.streakDays >= 7) {
            toUnlock.add(AchievementType.STREAK_7)
        }
        if (AchievementType.STREAK_30 !in currentUnlocked && stats.streakDays >= 30) {
            toUnlock.add(AchievementType.STREAK_30)
        }
        if (AchievementType.FIRST_INTERNSHIP !in currentUnlocked && stats.applicationsSubmitted >= 1) {
            toUnlock.add(AchievementType.FIRST_INTERNSHIP)
        }
        if (AchievementType.FIRST_CERTIFICATION !in currentUnlocked && stats.certificationsCompleted >= 1) {
            toUnlock.add(AchievementType.FIRST_CERTIFICATION)
        }
        if (AchievementType.TEN_APPLICATIONS !in currentUnlocked && stats.applicationsSubmitted >= 10) {
            toUnlock.add(AchievementType.TEN_APPLICATIONS)
        }
        if (AchievementType.FIVE_CERTS !in currentUnlocked && stats.certificationsCompleted >= 5) {
            toUnlock.add(AchievementType.FIVE_CERTS)
        }
        if (AchievementType.MASTER_CODER !in currentUnlocked && stats.problemsSolved >= 50) {
            toUnlock.add(AchievementType.MASTER_CODER)
        }

        return toUnlock
    }
}
