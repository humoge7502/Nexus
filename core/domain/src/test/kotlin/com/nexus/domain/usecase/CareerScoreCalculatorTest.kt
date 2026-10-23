package com.nexus.domain.usecase

import com.nexus.domain.model.Achievement
import com.nexus.domain.model.AchievementType
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class CareerScoreCalculatorTest {
    private lateinit var calculator: CareerScoreCalculator

    @Before
    fun setUp() { calculator = CareerScoreCalculator() }

    @Test
    fun `minimum inputs returns minimum score`() {
        val score = calculator.calculate(0f, 0f, 0, 0, emptyList(), 0)
        assertEquals(0, score.overallScore)
    }

    @Test
    fun `maximum inputs returns max score`() {
        val achievements = List(20) { Achievement("$it", "Test", "Test",
            AchievementType.FIRST_INTERNSHIP, isUnlocked = true, xpReward = 100) }
        val score = calculator.calculate(1f, 1f, 365, 300, achievements, 20)
        assertEquals(1000, score.overallScore)
    }

    @Test
    fun `half inputs returns mid range score`() {
        val score = calculator.calculate(0.5f, 0.5f, 180, 150, emptyList(), 10)
        assertTrue(score.overallScore in 200..800)
    }
}
