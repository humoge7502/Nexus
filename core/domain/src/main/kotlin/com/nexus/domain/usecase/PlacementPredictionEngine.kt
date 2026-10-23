package com.nexus.domain.usecase

import com.nexus.domain.model.ApplicationStatus
import com.nexus.domain.model.ScoreSnapshot
import com.nexus.domain.model.CohortEntry
import com.nexus.domain.model.PlacementForecast
import com.nexus.domain.repository.IScoreSnapshotRepository
import com.nexus.domain.repository.ICohortRepository
import kotlinx.coroutines.flow.first
import kotlin.math.abs
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PlacementPredictionEngine @Inject constructor(
  private val scoreSnapshotRepo: IScoreSnapshotRepository,
  private val cohortRepo: ICohortRepository,
) {

  data class Forecast(
    val tierLabel: String,
    val companyTier: String,
    val probabilityPct: Int,
    val scoreGap: Int,
    val gapItems: List<String>,
    val velocityTrend: String,
    val cohortBenchmark: String,
    val coachMessage: String,
  )

  companion object {
    val TIER_SCORES = mapOf(
      "TIER_1_PRODUCT" to 750,
      "TIER_2_PRODUCT" to 650,
      "TIER_3_SERVICE" to 500,
      "TIER_1_SERVICE" to 450,
    )
    const val NATIONAL_AVG_PLACEMENT = 0.65f
    const val PROB_CAP = 95
  }

  suspend fun generateForecasts(
    userId: String,
    currentScore: Int,
    collegeId: String,
    semester: String,
  ): List<Forecast> {
    val snapshots = scoreSnapshotRepo.getRecentSnapshots(userId, limit = 60).first()
    val velocity = computeVelocity(snapshots)
    val velocityFactor = when {
      velocity.trend == "rising" -> 1.15f
      velocity.trend == "declining" -> 0.85f
      else -> 1.0f
    }

    val cohortEntries = cohortRepo.getByCollege(collegeId).first()
    val cohortByTier = cohortEntries.associateBy { it.companyTier }

    return TIER_SCORES.map { (tier, targetScore) ->
      val readiness = (currentScore.toFloat() / targetScore).coerceAtMost(1f)
      val cohortEntry = cohortByTier[tier]
      val peerRate = cohortEntry?.let {
        if (it.totalStudents > 0) it.placedCount.toFloat() / it.totalStudents else null
      } ?: NATIONAL_AVG_PLACEMENT
      val peerFactor = (peerRate / NATIONAL_AVG_PLACEMENT).coerceIn(0.6f, 1.4f)

      val rawProb = readiness * velocityFactor * peerFactor * 100
      val probability = rawProb.toInt().coerceIn(0, PROB_CAP)

      val gap = (targetScore - currentScore).coerceAtLeast(0)
      val gapItems = mutableListOf<String>()
      if (gap > 50) gapItems.add("NEXUS Score is $gap points below tier threshold ($targetScore)")
      if (velocity.delta30d < -20) gapItems.add("Score declined ${abs(velocity.delta30d)} pts in 30 days")
      if (velocity.trend == "flat" && readiness < 0.6f) gapItems.add("Score plateau detected while still below target")

      val peerStr = if (cohortEntry != null && cohortEntry.totalStudents > 0) {
        "${cohortEntry.placedCount}/${cohortEntry.totalStudents} placed (${(peerRate * 100).toInt()}%)"
      } else "National average (${(NATIONAL_AVG_PLACEMENT * 100).toInt()}%)"

      Forecast(
        tierLabel = tier.replace("_", " "),
        companyTier = tier,
        probabilityPct = probability,
        scoreGap = gap,
        gapItems = gapItems,
        velocityTrend = velocity.trend,
        cohortBenchmark = peerStr,
        coachMessage = when {
          probability >= 80 -> "Strong placement readiness for $tier. Maintain trajectory and focus on interview rounds."
          probability >= 50 -> "On track but needs focused push. Address gap items to cross ${PROB_CAP}% threshold."
          else -> "Significant preparation gap for $tier. Prioritize DSA and skill building this placement season."
        }
      )
    }
  }

  data class VelocityResult(val trend: String, val delta30d: Int, val slope: Float)

  private fun computeVelocity(snapshots: List<ScoreSnapshot>): VelocityResult {
    if (snapshots.size < 2) {
      return VelocityResult("unknown", 0, 0f)
    }
    val latest = snapshots.last().score
    val prev30d = snapshots.filter { it.recordedAt <= snapshots.last().recordedAt - 30 * 86400000L }
    val baseline = if (prev30d.isNotEmpty()) prev30d.last().score else snapshots.first().score
    val delta = latest - baseline
    val slope = if (snapshots.size >= 3) {
      val points = snapshots.takeLast(5).mapIndexed { i, s -> i.toFloat() to s.score.toFloat() }
      val n = points.size.toFloat()
      val sumX = points.sumOf { it.first.toDouble() }.toFloat()
      val sumY = points.sumOf { it.second.toDouble() }.toFloat()
      val sumXY = points.sumOf { (it.first * it.second).toDouble() }.toFloat()
      val sumX2 = points.sumOf { (it.first * it.first).toDouble() }.toFloat()
      val denom = n * sumX2 - sumX * sumX
      if (denom != 0f) (n * sumXY - sumX * sumY) / denom else 0f
    } else 0f

    return VelocityResult(
      trend = when {
        delta > 30 -> "rising"
        delta < -15 -> "declining"
        delta in -15..30 -> "flat"
        else -> "stable"
      },
      delta30d = delta,
      slope = slope,
    )
  }
}
