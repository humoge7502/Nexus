package com.nexus.domain.usecase

import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CareerMirrorGeneratorTest {

    private val generator = CareerMirrorGenerator()

    @Suppress("LongParameterList")
    private fun input(
        currentScore: Int = 500,
        previousScore: Int? = 400,
        streakDays: Int = 30,
        dsaSolvedThisPeriod: Int = 40,
        hardSolved: Int = 5,
        applicationsThisPeriod: Int = 12,
        interviewsGot: Int = 2,
        offersReceived: Int = 0,
        skillsGained: List<String> = listOf("Kotlin"),
        missedOpportunityCount: Int = 0,
        avgDailyPracticeMinutes: Float = 90f,
    ) = CareerMirrorGenerator.MirrorInput(
        userId = "user-1",
        currentScore = currentScore,
        previousScore = previousScore,
        streakDays = streakDays,
        dsaSolvedThisPeriod = dsaSolvedThisPeriod,
        dsaByDifficulty = mapOf("EASY" to 20, "MEDIUM" to 15, "HARD" to hardSolved),
        applicationsThisPeriod = applicationsThisPeriod,
        interviewsGot = interviewsGot,
        offersReceived = offersReceived,
        skillsGained = skillsGained,
        goalsCompleted = 2,
        milestonesCompleted = 3,
        certificationsCompleted = 1,
        aiMessagesSent = 40,
        topWeaknesses = listOf("system design"),
        missedOpportunityCount = missedOpportunityCount,
        avgDailyPracticeMinutes = avgDailyPracticeMinutes,
    )

    @Test
    fun `mirror captures the period window and identity`() = runTest {
        val mirror = generator.generateMirror(input())

        assertEquals("user-1", mirror.userId)
        assertTrue(mirror.periodLabel.contains("–"))
        assertTrue(mirror.periodEnd - mirror.periodStart > 0)
        assertTrue(mirror.id.isNotBlank())
    }

    @Test
    fun `strong quarter celebrates every signal`() = runTest {
        val mirror = generator.generateMirror(
            input(
                currentScore = 700,
                previousScore = 500,
                streakDays = 75,
                dsaSolvedThisPeriod = 120,
                offersReceived = 2,
                missedOpportunityCount = 8,
                skillsGained = listOf("Kotlin", "System Design", "SQL"),
            ),
        )

        assertEquals(5, mirror.keyInsights.size)
        assertTrue(mirror.keyInsights.first().contains("Excellent growth: NEXUS Score improved by +200 pts"))
        assertTrue(mirror.keyInsights.any { it.contains("DSA momentum: 120 problems") })
        assertTrue(mirror.keyInsights.any { it.contains("2 offer(s)") })
        assertTrue(mirror.actionItems == listOf("Maintain velocity — strong trajectory building"))

        val narrative = mirror.narrative
        assertTrue(narrative.startsWith("Your placement readiness has been accelerating rapidly over the past 90 days."))
        assertTrue(narrative.contains("consistently (12 applications)"))
        assertTrue(narrative.contains("received 2 interview calls, and earned 2 offer(s)"))
    }

    @Test
    fun `declining quarter is described honestly`() = runTest {
        val mirror = generator.generateMirror(
            input(
                currentScore = 460,
                previousScore = 500,
                streakDays = 3,
                dsaSolvedThisPeriod = 10,
                hardSolved = 1,
                applicationsThisPeriod = 2,
                interviewsGot = 0,
                skillsGained = emptyList(),
                avgDailyPracticeMinutes = 20f,
            ),
        )

        assertTrue(mirror.keyInsights.single().contains("Score declined by 40 pts"))
        assertTrue(mirror.narrative.contains("has been slipping over the past 90 days"))
        assertTrue(mirror.narrative.contains("building momentum (2 applications)"))
        assertEquals(
            listOf(
                "Target: Solve ≥3 HARD DSA problems (signals Tier-1 readiness)",
                "Apply to ≥15 new opportunities this month",
                "Complete at least 1 certification to demonstrate skill depth",
                "Increase daily prep to 60+ min for placement-grade readiness",
            ),
            mirror.actionItems,
        )
    }

    @Test
    fun `plateau quarter nudges a strategy shift`() = runTest {
        val mirror = generator.generateMirror(
            input(
                currentScore = 510,
                previousScore = 500,
                streakDays = 10,
                dsaSolvedThisPeriod = 30,
                applicationsThisPeriod = 6,
                interviewsGot = 1,
                avgDailyPracticeMinutes = 45f,
            ),
        )

        assertTrue(mirror.keyInsights.any { it.contains("Score plateau") })
        assertTrue(mirror.narrative.contains("has been plateaued over the past 90 days"))
        assertEquals("building momentum (6 applications)", mirror.narrative.substringAfter("You applied ").substringBefore(", received"))
    }

    @Test
    fun `first mirror treats the current score as the baseline`() = runTest {
        val mirror = generator.generateMirror(input(previousScore = null))

        assertTrue(mirror.keyInsights.first().contains("Score plateau"))
        assertTrue(mirror.narrative.contains("plateaued"))
    }
}
