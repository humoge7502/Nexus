package com.nexus.domain.usecase

import com.nexus.domain.model.ConfidenceAssessment
import com.nexus.domain.model.InterviewOutcome
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ConfidenceCalibrationEngineTest {

    private val engine = ConfidenceCalibrationEngine()

    private fun assessment(confidence: Float, actual: Float?) = ConfidenceAssessment(
        id = "a-$confidence-$actual",
        assessmentType = "DSA",
        confidenceScore = confidence,
        actualScore = actual,
    )

    private fun outcome(result: String) = InterviewOutcome(
        id = "o-$result-$result",
        company = "Acme",
        role = "SDE Intern",
        interviewRound = "Technical",
        result = result,
    )

    @Test
    fun `no recorded outcomes returns null`() {
        assertNull(engine.evaluateCalibration("DSA", emptyList()))
        assertNull(engine.evaluateCalibration("DSA", listOf(assessment(0.8f, null))))
    }

    @Test
    fun `overconfidence is detected`() {
        val result = engine.evaluateCalibration(
            "DSA",
            listOf(assessment(0.9f, 0.4f), assessment(0.8f, 0.3f), assessment(0.9f, 0.5f)),
        )
        requireNotNull(result)
        assertEquals("overconfident", result.biasDirection)
        assertEquals(0.4666f, result.avgBias, 0.001f)
        assertEquals(3, result.sampleSize)
        assertTrue(result.message.contains("pts higher"))
        assertTrue(result.recommendation.contains("cold self-assessment"))
    }

    @Test
    fun `underconfidence is detected`() {
        val result = engine.evaluateCalibration(
            "DSA",
            listOf(assessment(0.3f, 0.9f), assessment(0.4f, 0.8f), assessment(0.2f, 0.7f)),
        )
        requireNotNull(result)
        assertEquals("underconfident", result.biasDirection)
        assertTrue(result.avgBias < 0f)
        assertTrue(result.message.contains("pts below"))
        assertTrue(result.recommendation.contains("Trust your preparation data"))
    }

    @Test
    fun `aligned assessments are well calibrated`() {
        val result = engine.evaluateCalibration(
            "DSA",
            listOf(assessment(0.7f, 0.65f), assessment(0.6f, 0.6f), assessment(0.8f, 0.75f)),
        )
        requireNotNull(result)
        assertEquals("well-calibrated", result.biasDirection)
        assertTrue(result.recommendation.contains("competitive edge"))
    }

    @Test
    fun `small overconfident samples ask for more data`() {
        val result = engine.evaluateCalibration(
            "DSA",
            listOf(assessment(0.9f, 0.3f), assessment(0.8f, 0.4f)),
        )
        requireNotNull(result)
        assertEquals("overconfident", result.biasDirection)
        assertEquals(2, result.sampleSize)
        assertTrue(result.message.contains("More data needed"))
    }

    @Test
    fun `pre interview brief handles no history`() {
        assertEquals(
            "No past interviews logged yet.",
            engine.generatePreInterviewBrief(emptyList(), avgCalibration = 0f),
        )
    }

    @Test
    fun `pre interview brief summarizes selection rate case-insensitively`() {
        val brief = engine.generatePreInterviewBrief(
            listOf(outcome("selected"), outcome("SELECTED"), outcome("rejected"), outcome("Pending")),
            avgCalibration = 0f,
        )
        assertTrue(brief.startsWith("Your last 4 interviews: 2 selected (50% rate)."))
    }

    @Test
    fun `pre interview brief adds calibration coaching`() {
        val outcomes = listOf(outcome("SELECTED"), outcome("SELECTED"), outcome("REJECTED"))

        assertTrue(
            engine.generatePreInterviewBrief(outcomes, avgCalibration = 0.4f)
                .contains("tend to overestimate"),
        )
        assertTrue(
            engine.generatePreInterviewBrief(outcomes, avgCalibration = -0.4f)
                .contains("underestimate yourself"),
        )
        assertEquals(
            "Your last 3 interviews: 2 selected (66% rate).",
            engine.generatePreInterviewBrief(outcomes, avgCalibration = 0.1f),
        )
    }
}
