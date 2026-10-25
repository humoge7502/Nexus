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
    const val THIRTY_DAYS_MS = 30L * 24 * 60 * 60 * 1000
  }

  suspend fun generateForecasts(
    userId: String,
    currentScore: Int,
    collegeId: String,
    semester: String,
    extraFactors: List<String> = emptyList(),
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
        gapItems = (gapItems + extraFactors).distinct(),
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
    // The DAO returns newest-first; order explicitly so "latest" is correct
    // regardless of the data source.
    val ordered = snapshots.sortedBy { it.recordedAt }
    val latest = ordered.last()
    val cutoff = latest.recordedAt - THIRTY_DAYS_MS
    val baseline = ordered.lastOrNull { it.recordedAt <= cutoff }?.score ?: ordered.first().score
    val delta = latest.score - baseline
    val slope = if (ordered.size >= 3) {
      val points = ordered.takeLast(5).mapIndexed { i, s -> i.toFloat() to s.score.toFloat() }
      val n = points.size.toFloat()
      val sumX = points.sumOf { it.first.toDouble() }.toFloat()
      val sumY = points.sumOf { it.second.toDouble() }.toFloat()
      val sumXY = points.sumOf { (it.first * it.second).toDouble() }.toFloat()
      val sumX2 = points.sumOf { (it.first * it.first).toDouble() }.toFloat()
      val denom = n * sumX2 - sumX * sumX
      if (denom != 0f) (n * sumXY - sumX * sumY) / denom else 0f
    } else 0f

    val trend = when {
      delta > 30 -> "rising"
      delta < -15 -> "declining"
      else -> "flat"
    }

    return VelocityResult(trend = trend, delta30d = delta, slope = slope)
  }
}
