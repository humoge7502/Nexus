package com.nexus.domain.usecase

import kotlin.random.Random
import javax.inject.Inject

data class VariableReward(
    val multiplier: Float = 1f,
    val bonusXP: Int = 0,
    val message: String? = null,
)

class VariableRewardManager @Inject constructor() {

    fun computeReward(baseXP: Int, currentStreak: Int): VariableReward {
        var multiplier = 1f
        var message: String? = null
        val roll = Random.nextFloat()

        if (roll < 0.05f) {
            multiplier = 3f
            message = "JACKPOT! 3x XP bonus! \uD83C\uDF89"
        } else if (roll < 0.15f) {
            multiplier = 2f
            message = "2x XP bonus activated! \u26A1"
        }

        val bonusXP = if (currentStreak > 0 && currentStreak % 7 == 0 && roll < 0.3f) {
            50 + (currentStreak / 7) * 10
        } else {
            0
        }

        if (bonusXP > 0 && message == null) {
            message = "${currentStreak}-day streak reward: +$bonusXP bonus XP! \uD83C\uDF1F"
        }

        return VariableReward(
            multiplier = multiplier,
            bonusXP = bonusXP,
            message = message,
        )
    }
}
