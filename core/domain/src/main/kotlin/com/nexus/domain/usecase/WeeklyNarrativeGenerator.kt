package com.nexus.domain.usecase

import com.nexus.datastore.UserPreferences
import com.nexus.domain.repository.IDSAProblemRepository
import com.nexus.domain.repository.IStreakRepository
import com.nexus.domain.repository.IUserXpRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WeeklyNarrativeGenerator @Inject constructor(
  private val xpRepository: IUserXpRepository,
  private val dsaRepository: IDSAProblemRepository,
  private val streakRepository: IStreakRepository,
  private val userPreferences: UserPreferences,
) {

  suspend fun generate(): WeeklyNarrative {
    val totalXP = runCatching { xpRepository.totalXP.first() }.getOrDefault(0)
    val allProblems = runCatching { dsaRepository.getAllProblems().first() }.getOrDefault(emptyList())
    val weeklyDSA = allProblems.count { it.solvedAt != null }
    val streak = runCatching { streakRepository.currentStreak.first().currentStreak }.getOrDefault(0)

    val weeklyXP = runCatching {
      val snapshots = xpRepository.getSnapshotsForUser().first()
      if (snapshots.size >= 2) {
        val currentWeek = snapshots.lastOrNull()?.score ?: 0
        val previousWeek = snapshots.dropLast(1).lastOrNull()?.score ?: 0
        (currentWeek - previousWeek).coerceAtLeast(0)
      } else {
        totalXP
      }
    }.getOrDefault(totalXP)

    val previousWeekXP = runCatching { 
        val snapshots = xpRepository.getSnapshotsForUser().first()
        if (snapshots.size >= 3) {
            val twoWeeksAgo = snapshots.dropLast(2).lastOrNull()?.score ?: 0
            val previousWeek = snapshots.dropLast(1).lastOrNull()?.score ?: 0
            previousWeek - twoWeeksAgo
        } else {
            0
        }
    }.getOrDefault(0)

    val trend = when {
      previousWeekXP == 0 -> "starting"
      weeklyXP > previousWeekXP * 1.2f -> "accelerating"
      weeklyXP >= previousWeekXP * 0.8f -> "steady"
      else -> "declining"
    }

    val headline = when {
      trend == "accelerating" && weeklyXP > 1000 -> "Outstanding week — real momentum"
      trend == "accelerating" -> "Picking up speed"
      trend == "steady" && streak > 14 -> "Consistent — the most underrated quality"
      trend == "steady" -> "Solid week — keep the rhythm"
      trend == "declining" && streak > 7 -> "Slower week — your streak says you'll bounce back"
      trend == "declining" -> "Light week — what got in the way?"
      else -> "A new week of progress begins"
    }

    val body = buildString {
      val problemWord = if (weeklyDSA == 1) "problem" else "problems"
      append("This week: $weeklyXP XP, $weeklyDSA DSA $problemWord solved. ")
      when {
        previousWeekXP == 0 -> append("This is your baseline.")
        weeklyXP > previousWeekXP -> append("+${weeklyXP - previousWeekXP} XP more than last week.")
        weeklyXP < previousWeekXP -> append("${previousWeekXP - weeklyXP} XP less than last week.")
        else -> append("Same pace as last week.")
      }
      append(buildStreakContext(streak))
    }

    val callToAction = when {
      streak == 0 -> "Start a 7-day streak this week"
      weeklyDSA < 3 -> "Solve 3 DSA problems this week — minimum viable prep"
      else -> "Maintain your $streak-day streak — your most valuable asset"
    }

    return WeeklyNarrative(
      headline = headline,
      body = body,
      callToAction = callToAction,
      trend = trend,
      weeklyXP = weeklyXP,
      weeklyDSA = weeklyDSA,
    )
  }

  private fun buildStreakContext(streak: Int): String = when {
    streak >= 30 -> " Your $streak-day streak puts you in the top 5% of NEXUS users."
    streak >= 14 -> " Your $streak-day streak is genuinely impressive."
    streak >= 7 -> " Seven days in — you're building a real habit."
    streak > 0 -> " Day $streak — every streak starts here."
    else -> ""
  }
}

data class WeeklyNarrative(
  val headline: String,
  val body: String,
  val callToAction: String,
  val trend: String,
  val weeklyXP: Int,
  val weeklyDSA: Int,
)
