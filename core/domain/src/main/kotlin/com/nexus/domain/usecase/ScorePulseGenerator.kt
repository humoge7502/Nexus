package com.nexus.domain.usecase

import com.nexus.datastore.UserPreferences
import com.nexus.domain.model.ScorePulseEvent
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScorePulseGenerator @Inject constructor(
  private val userPrefs: UserPreferences,
) {

  companion object {
    private val MILESTONES = listOf(100, 200, 300, 400, 500, 600, 700, 750, 800, 850, 900, 950, 1000)
  }

  fun generatePulse(
    oldScore: Int,
    newScore: Int,
    triggerSource: String,
    triggerDetail: String,
  ): ScorePulseEvent? {
    val delta = newScore - oldScore
    if (delta <= 0) return null

    val nextMilestone = MILESTONES.firstOrNull { it > newScore } ?: 1000
    val pointsNeeded = nextMilestone - newScore
    val nextMsg = when (triggerSource) {
      "dsa_problem" -> "$pointsNeeded more points → score $nextMilestone"
      "career_hit" -> "Daily hit tomorrow → maintain momentum"
      "interview_logged" -> "Log interview feedback → unlock insights"
      else -> "$pointsNeeded points to score $nextMilestone"
    }

    return ScorePulseEvent(
      oldScore = oldScore,
      newScore = newScore,
      delta = delta,
      triggerSource = triggerSource,
      triggerDetail = triggerDetail,
      percentileMessage = "",
      nextMilestone = nextMsg,
    )
  }
}
