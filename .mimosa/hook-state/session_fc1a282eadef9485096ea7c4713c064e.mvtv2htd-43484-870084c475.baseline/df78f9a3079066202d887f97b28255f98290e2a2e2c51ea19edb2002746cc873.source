package com.nexus.domain.usecase

import com.nexus.domain.model.CareerMirror
import kotlin.math.abs
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CareerMirrorGenerator @Inject constructor() {

  companion object {
    const val PERIOD_DAYS = 90L
    val DATE_FMT: DateTimeFormatter = DateTimeFormatter.ofPattern("MMM d, yyyy")
  }

  data class MirrorInput(
    val userId: String,
    val currentScore: Int,
    val previousScore: Int?,
    val streakDays: Int,
    val dsaSolvedThisPeriod: Int,
    val dsaByDifficulty: Map<String, Int>,
    val applicationsThisPeriod: Int,
    val interviewsGot: Int,
    val offersReceived: Int,
    val skillsGained: List<String>,
    val goalsCompleted: Int,
    val milestonesCompleted: Int,
    val certificationsCompleted: Int,
    val aiMessagesSent: Int,
    val topWeaknesses: List<String>,
    val missedOpportunityCount: Int,
    val avgDailyPracticeMinutes: Float,
  )

  suspend fun generateMirror(input: MirrorInput): CareerMirror {
    val now = System.currentTimeMillis()
    val periodEnd = now
    val periodStart = now - PERIOD_DAYS * 86400000L
    val startDate = Instant.ofEpochMilli(periodStart).atZone(ZoneId.systemDefault()).toLocalDate()
    val endDate = Instant.ofEpochMilli(periodEnd).atZone(ZoneId.systemDefault()).toLocalDate()
    val startLabel = startDate.format(DATE_FMT)
    val endLabel = endDate.format(DATE_FMT)

    val scoreDelta = input.currentScore - (input.previousScore ?: input.currentScore)
    val insights = generateInsights(input, scoreDelta)
    val actionItems = generateActionItems(input)

    return CareerMirror(
      id = UUID.randomUUID().toString(),
      userId = input.userId,
      periodStart = periodStart,
      periodEnd = periodEnd,
      periodLabel = "$startLabel – $endLabel",
      narrative = generateNarrative(input, scoreDelta, insights),
      keyInsights = insights,
      actionItems = actionItems,
      createdAt = now,
    )
  }

  private fun generateInsights(input: MirrorInput, scoreDelta: Int): List<String> {
    val list = mutableListOf<String>()
    list += when {
      scoreDelta >= 50 -> "Excellent growth: NEXUS Score improved by +$scoreDelta pts this quarter"
      scoreDelta >= 20 -> "Steady progress: Score grew by +$scoreDelta pts this quarter"
      scoreDelta >= 0 -> "Score plateau: Only +$scoreDelta pts of growth — time to shift strategy"
      else -> "Score declined by ${abs(scoreDelta)} pts — review preparation intensity"
    }
    if (input.dsaSolvedThisPeriod > 80) list += "DSA momentum: ${input.dsaSolvedThisPeriod} problems solved this period"
    if (input.interviewsGot > 0) list += "${input.interviewsGot} interviews received — gaining visibility"
    if (input.offersReceived > 0) list += "${input.offersReceived} offer(s) this period — strong outcome"
    if (input.missedOpportunityCount > 5) list += "${input.missedOpportunityCount} saved opportunities not yet applied to"
    if (input.skillsGained.isNotEmpty()) list += "New skills: ${input.skillsGained.take(3).joinToString(", ")}"
    if (input.streakDays >= 60) list += "Consistency: ${input.streakDays}-day streak maintained"
    return list.take(5)
  }

  private fun generateActionItems(input: MirrorInput): List<String> {
    val items = mutableListOf<String>()
    val hardCount = input.dsaByDifficulty["HARD"] ?: 0
    if (hardCount < 3) items += "Target: Solve ≥3 HARD DSA problems (signals Tier-1 readiness)"
    if (input.interviewsGot == 0) items += "Apply to ≥15 new opportunities this month"
    if (input.skillsGained.isEmpty()) items += "Complete at least 1 certification to demonstrate skill depth"
    if (input.avgDailyPracticeMinutes < 60f) items += "Increase daily prep to 60+ min for placement-grade readiness"
    return items.take(4).ifEmpty { listOf("Maintain velocity — strong trajectory building") }
  }

  private fun generateNarrative(input: MirrorInput, scoreDelta: Int, insights: List<String>): String {
    val direction = when {
      scoreDelta >= 50 -> "accelerating rapidly"
      scoreDelta >= 20 -> "making steady progress"
      scoreDelta >= 0 -> "plateaued"
      else -> "slipping"
    }
    val pace = when {
      input.applicationsThisPeriod >= 20 -> "actively"
      input.applicationsThisPeriod >= 10 -> "consistently"
      else -> "building momentum"
    }
    return buildString {
      append("Your placement readiness has been $direction over the past 90 days. ")
      append("You applied $pace (${input.applicationsThisPeriod} applications), ")
      append("received ${input.interviewsGot} interview calls")
      if (input.offersReceived > 0) append(", and earned ${input.offersReceived} offer(s)")
      append(". ")
      append(insights.firstOrNull() ?: "Keep pushing — every solved problem is a step toward your goal.")
    }
  }
}
