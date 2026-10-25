package com.nexus.domain.usecase

import com.nexus.domain.usecase.PlacementReadinessModel.Features
import kotlin.math.ln
import kotlin.math.min

/**
 * Maps raw app signals to the exact feature space used by
 * [PlacementReadinessModel]. Kept separate from the generated file so the
 * training pipeline can be rerun without touching this mapping.
 */
object PlacementReadinessFeatures {

  private const val MAX_SKILLS = 20f

  data class RawSignals(
    val nexusScore: Int = 500,
    val dsaSolved: Int = 0,
    val dsaHardSolved: Int = 0,
    val dsaMediumSolved: Int = 0,
    val dsaAttempted: Int = 0,
    val applications: Int = 0,
    val interviews: Int = 0,
    val offers: Int = 0,
    val certifications: Int = 0,
    val projects: Int = 0,
    val streakDays: Int = 0,
    val skillCount: Int = 0,
    val mockInterviews: Int = 0,
    val collegeTier: String? = null,
  )

  fun fromRaw(raw: RawSignals): Features {
    val solved = raw.dsaSolved.coerceAtLeast(0)
    val apps = raw.applications.coerceAtLeast(0)
    val attempts = maxOf(solved + raw.dsaAttempted.coerceAtLeast(0), 1)

    return Features(
      dsaSolvedLog = log1p(solved.toFloat()),
      dsaHardRatio = if (solved > 0) raw.dsaHardSolved.toFloat() / solved else 0f,
      dsaMediumRatio = if (solved > 0) raw.dsaMediumSolved.toFloat() / solved else 0f,
      applicationsLog = log1p(apps.toFloat()),
      interviewRate = if (apps > 0) (raw.interviews.toFloat() / apps).coerceIn(0f, 1f) else 0f,
      offerRate = if (apps > 0) (raw.offers.toFloat() / apps).coerceIn(0f, 1f) else 0f,
      certsLog = log1p(raw.certifications.coerceAtLeast(0).toFloat()),
      projectsLog = log1p(raw.projects.coerceAtLeast(0).toFloat()),
      streakLog = log1p(raw.streakDays.coerceAtLeast(0).toFloat()),
      skillsNorm = min(raw.skillCount.coerceAtLeast(0), MAX_SKILLS.toInt()) / MAX_SKILLS,
      mockInterviewsLog = log1p(raw.mockInterviews.coerceAtLeast(0).toFloat()),
      scoreNorm = (raw.nexusScore.coerceIn(0, 1000) / 1000f),
      collegeTierNorm = collegeTierNorm(raw.collegeTier),
      attemptsLog = log1p(attempts.toFloat()),
    )
  }

  /**
   * Mirrors the training column `(5 - tier) / 4` for tiers 1..4. Unknown or
   * blank tiers fall back to tier 3, the neutral cohort.
   */
  fun collegeTierNorm(value: String?): Float {
    val normalized = value?.trim()?.uppercase() ?: return 0.5f
    val tier = when {
      normalized.contains("1") -> 1
      normalized.contains("2") -> 2
      normalized.contains("3") -> 3
      normalized.contains("4") -> 4
      else -> 3
    }
    return (5 - tier) / 4f
  }

  /** log(1 + x) without pulling in kotlin.math.log1p (API 26 keeps this local). */
  private fun log1p(value: Float): Float = ln(1f + value)
}
