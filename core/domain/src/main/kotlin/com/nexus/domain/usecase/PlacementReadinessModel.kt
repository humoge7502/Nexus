// GENERATED FILE — do not edit by hand.
// Source: ml/placement_readiness/train.py
// Model: logistic v1.0.0
// Trained: 2026-10-24T17:56:16.002385+00:00
// Dataset: placement_cohort_v1.csv (24000 rows, sha256 7625e344a42e5a11...)
// Test ROC-AUC 0.8614, Brier 0.1521
// The training cohort is synthetic; see ml/placement_readiness/README.md.

package com.nexus.domain.usecase

import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.roundToInt

object PlacementReadinessModel {

  const val VERSION = "1.0.0"
  const val FEATURE_COUNT = 14
  const val TEMPERATURE = 1.0f
  private val MEANS = floatArrayOf(2.33642476f, 0.23426341f, 0.36428480f, 1.87927319f, 0.26621211f, 0.15513641f, 0.73904587f, 0.98072545f, 2.09755342f, 0.31692187f, 1.16661480f, 0.49828192f, 0.62384115f, 2.74710768f)
  private val SCALES = floatArrayOf(0.93244380f, 0.14049215f, 0.23127653f, 0.55125868f, 0.29335786f, 0.23734161f, 0.54497585f, 0.62818118f, 0.76336797f, 0.19037683f, 0.61510093f, 0.12319263f, 0.27954103f, 0.65211610f)
  private val WEIGHTS = floatArrayOf(0.10408635f, 0.14698341f, 0.00911159f, -0.06849068f, 0.28839256f, 0.14847510f, -0.03756948f, -0.01714499f, -0.02719990f, -0.04497105f, 0.27077245f, 1.44594510f, 0.12194810f, 0.04862957f)
  private const val BIAS = 0.07000804f
  private val FEATURE_LABELS = arrayOf(
    "DSA problems solved",
    "Hard-problem share",
    "Medium-problem share",
    "Applications submitted",
    "Interview conversion",
    "Offer conversion",
    "Certifications",
    "Portfolio projects",
    "Study streak",
    "Skills on profile",
    "Mock interviews",
    "NEXUS score",
    "College tier",
    "Practice attempts",
  )
  /** Transformed inputs; build them with [PlacementReadinessFeatures.fromRaw]. */
  data class Features(
    val dsaSolvedLog: Float = 0f,
    val dsaHardRatio: Float = 0f,
    val dsaMediumRatio: Float = 0f,
    val applicationsLog: Float = 0f,
    val interviewRate: Float = 0f,
    val offerRate: Float = 0f,
    val certsLog: Float = 0f,
    val projectsLog: Float = 0f,
    val streakLog: Float = 0f,
    val skillsNorm: Float = 0f,
    val mockInterviewsLog: Float = 0f,
    val scoreNorm: Float = 0f,
    val collegeTierNorm: Float = 0f,
    val attemptsLog: Float = 0f,
  ) {
    fun toFloatArray(): FloatArray = floatArrayOf(
      dsaSolvedLog, dsaHardRatio, dsaMediumRatio, applicationsLog, interviewRate, offerRate, certsLog, projectsLog, streakLog, skillsNorm, mockInterviewsLog, scoreNorm, collegeTierNorm, attemptsLog
    )
  }

  data class Factor(val label: String, val deltaPoints: Int)

  private fun featuresOf(v: FloatArray): Features = Features(
      dsaSolvedLog = v[0],
      dsaHardRatio = v[1],
      dsaMediumRatio = v[2],
      applicationsLog = v[3],
      interviewRate = v[4],
      offerRate = v[5],
      certsLog = v[6],
      projectsLog = v[7],
      streakLog = v[8],
      skillsNorm = v[9],
      mockInterviewsLog = v[10],
      scoreNorm = v[11],
      collegeTierNorm = v[12],
      attemptsLog = v[13],
  )


  /** Probability in 0..1 that the student is placed this season. */
  fun predict(features: Features): Float {
    val source = features.toFloatArray()
    val x = FloatArray(FEATURE_COUNT) { i ->
      (source[i] - MEANS[i]) / SCALES[i]
    }
    val logit = forward(x)
    return sigmoid(logit / TEMPERATURE)
  }

  fun predictPercent(features: Features): Int =
    (predict(features) * 100f).roundToInt().coerceIn(1, 99)

  /**
   * Local explanation: moves each input one standardized unit and ranks the
   * absolute probability change. Deterministic, dependency-free (no SHAP).
   */
  fun topFactors(features: Features, limit: Int = 3): List<Factor> {
    val base = predict(features)
    val raw = features.toFloatArray()
    return FEATURE_LABELS.mapIndexed { i, label ->
      val shifted = raw.copyOf()
      shifted[i] = shifted[i] + SCALES[i]
      val delta = predict(featuresOf(shifted)) - base
      Factor(label, (delta * 100f).roundToInt())
    }
      .sortedByDescending { abs(it.deltaPoints) }
      .take(limit)
  }

  private fun sigmoid(value: Float): Float =
    1f / (1f + exp(-value))

  private fun forward(input: FloatArray): Float {
    var sum = BIAS
    for (i in WEIGHTS.indices) sum += WEIGHTS[i] * input[i]
    return sum
  }

}
