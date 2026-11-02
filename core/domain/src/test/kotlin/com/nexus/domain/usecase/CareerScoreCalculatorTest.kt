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

    @Test
    fun `ledger lines sum to the headline score`() {
        val score = calculator.calculate(0.37f, 0.62f, 41, 87, emptyList(), 5)
        assertEquals(score.components.sumOf { it.earned }, score.overallScore)
    }

    @Test
    fun `every ledger line names the weight it is measured against`() {
        val score = calculator.calculate(0.5f, 0.5f, 100, 100, emptyList(), 10)
        assertEquals(1000, score.components.sumOf { it.available })
        assertTrue(score.components.all { it.earned in 0..it.available })
    }

    @Test
    fun `over-range fractions cannot mint points`() {
        val score = calculator.calculate(2f, 2f, 9999, 9999, emptyList(), 9999)
        assertTrue(score.overallScore <= 1000)
        assertTrue(score.components.all { it.earned <= it.available })
    }
}
