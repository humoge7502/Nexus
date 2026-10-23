package com.nexus.domain.usecase

import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class StreakCalculatorTest {
    private lateinit var calculator: StreakCalculator
    private val today = LocalDate.of(2025, 5, 27)

    @Before
    fun setUp() {
        val clock = Clock.fixed(today.atStartOfDay(ZoneId.systemDefault()).toInstant(), ZoneId.systemDefault())
        calculator = StreakCalculator(clock)
    }

    @Test
    fun `empty dates returns zero streak`() {
        val result = calculator.calculateStreak(emptyList())
        assertEquals(0, result.currentStreak)
        assertEquals(0, result.longestStreak)
        assertFalse(result.isActive)
    }

    @Test
    fun `single today date returns streak of 1`() {
        val result = calculator.calculateStreak(listOf(today))
        assertEquals(1, result.currentStreak)
        assertEquals(1, result.longestStreak)
        assertTrue(result.isActive)
    }

    @Test
    fun `consecutive 7 days returns streak of 7`() {
        val dates = (0..6).map { today.minusDays(it.toLong()) }
        val result = calculator.calculateStreak(dates)
        assertEquals(7, result.currentStreak)
        assertEquals(7, result.longestStreak)
    }

    @Test
    fun `broken streak resets current but remembers longest`() {
        val dates = listOf(today, today.minusDays(1), today.minusDays(3), today.minusDays(4), today.minusDays(5))
        val result = calculator.calculateStreak(dates)
        assertEquals(2, result.currentStreak)
        assertEquals(3, result.longestStreak)
    }

    @Test
    fun `last active 2 days ago means streak broken`() {
        val dates = listOf(today.minusDays(2))
        val result = calculator.calculateStreak(dates)
        assertEquals(0, result.currentStreak)
        assertFalse(result.isActive)
    }

    @Test
    fun `interleaved dates find correct longest streak`() {
        val dates = listOf(today, today.minusDays(1), today.minusDays(2),
            today.minusDays(10), today.minusDays(11), today.minusDays(12), today.minusDays(13))
        val result = calculator.calculateStreak(dates)
        assertEquals(3, result.currentStreak)
        assertEquals(4, result.longestStreak)
    }
}
