package com.nexus.domain.usecase

import com.nexus.domain.model.ConfidenceAssessment
import com.nexus.domain.model.InterviewOutcome
import kotlin.math.abs
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConfidenceCalibrationEngine @Inject constructor() {

  data class CalibrationResult(
    val type: String,
    val avgBias: Float,
    val biasDirection: String,
    val message: String,
    val recommendation: String,
    val sampleSize: Int,
  )

  private data class AssessmentPair(
    val confidence: Float,
    val actual: Float,
  )

  fun evaluateCalibration(
    type: String,
    assessments: List<ConfidenceAssessment>,
  ): CalibrationResult? {
    val paired = assessments.mapNotNull { a ->
      a.actualScore?.let { actual -> AssessmentPair(a.confidenceScore, actual) }
    }
    if (paired.isEmpty()) return null

    val avgBias = paired.map { (c, a) -> c - a }.average().toFloat()
    val biasDirection = when {
      avgBias > 0.3f -> "overconfident"
      avgBias < -0.3f -> "underconfident"
      else -> "well-calibrated"
    }
    val sample = paired.size

    val (message, recommendation) = when {
      biasDirection == "overconfident" && sample >= 3 ->
        "You consistently rate yourself ${avgBias.toInt()} pts higher than your actual performance. This can lead to underpreparation." to
          "Before each interview, do a cold self-assessment. Compare to past outcomes to recalibrate."
      biasDirection == "underconfident" && sample >= 3 ->
        "You rate yourself ${abs(avgBias).toInt()} pts below your real performance. You may be leaving opportunities on the table." to
          "Trust your preparation data more. NEXUS Score trends show stronger readiness than you report."
      biasDirection == "well-calibrated" ->
        "Your self-assessment is closely aligned with actual outcomes." to
          "Maintain this calibration — it's a competitive edge in interview negotiations."
      else ->
        "More data needed (have $sample data points)." to
          "Log self-assessments before every mock interview to build your calibration profile."
    }

    return CalibrationResult(type, avgBias, biasDirection, message, recommendation, sample)
  }

  fun generatePreInterviewBrief(
    recentOutcomes: List<InterviewOutcome>,
    avgCalibration: Float,
  ): String {
    val pattern = if (recentOutcomes.isNotEmpty()) {
      val selected = recentOutcomes.count { it.result.equals("SELECTED", true) }
      val total = recentOutcomes.size
      val rate = (selected.toFloat() / total * 100).toInt()
      "Your last $total interviews: $selected selected ($rate% rate)"
    } else "No past interviews logged yet"

    val calibrationNote = if (avgCalibration > 0.3f) {
      " You tend to overestimate your interview readiness — do 1 extra mock before today."
    } else if (avgCalibration < -0.3f) {
      " You underestimate yourself — trust your prep, you're likely better prepared than you think."
    } else ""

    return "$pattern.$calibrationNote"
  }
}
