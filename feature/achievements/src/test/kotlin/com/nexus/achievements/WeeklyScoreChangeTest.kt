package com.nexus.achievements

import com.nexus.domain.model.ScoreSnapshot
import org.junit.Assert.assertEquals
import org.junit.Test

class WeeklyScoreChangeTest {

    private val now = 1_700_000_000_000L
    private val dayMs = 24L * 60 * 60 * 1000

    private fun snapshot(score: Int, daysAgo: Long) = ScoreSnapshot(
        id = "s-$daysAgo",
        userId = "user",
        score = score,
        recordedAt = now - daysAgo * dayMs,
    )

    @Test
    fun `no history returns zero`() {
        assertEquals(0, weeklyChangeOf(emptyList(), currentScore = 420, now = now))
    }

    @Test
    fun `only todays snapshot returns zero`() {
        val history = listOf(snapshot(420, daysAgo = 0))
        assertEquals(0, weeklyChangeOf(history, currentScore = 420, now = now))
    }

    @Test
    fun `gain since oldest snapshot in window`() {
        val history = listOf(
            snapshot(300, daysAgo = 6),
            snapshot(380, daysAgo = 3),
            snapshot(420, daysAgo = 0),
        )
        assertEquals(120, weeklyChangeOf(history, currentScore = 420, now = now))
    }

    @Test
    fun `snapshots older than a week are ignored`() {
        val history = listOf(snapshot(100, daysAgo = 9))
        assertEquals(0, weeklyChangeOf(history, currentScore = 420, now = now))
    }

    @Test
    fun `negative change is reported`() {
        val history = listOf(snapshot(500, daysAgo = 2))
        assertEquals(-80, weeklyChangeOf(history, currentScore = 420, now = now))
    }

    @Test
    fun `zero current score returns zero`() {
        val history = listOf(snapshot(500, daysAgo = 2))
        assertEquals(0, weeklyChangeOf(history, currentScore = 0, now = now))
    }
}
