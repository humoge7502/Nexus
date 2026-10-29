package com.nexus.growth

import org.junit.Assert.assertEquals
import org.junit.Test

class ArenaRankingTest {

    @Test
    fun `unranked or empty board has no percentile`() {
        assertEquals(0, topPercentOf(0, 10))
        assertEquals(0, topPercentOf(3, 0))
        assertEquals(0, topPercentOf(-1, 10))
    }

    @Test
    fun `rank one of ten is top ten percent`() {
        assertEquals(10, topPercentOf(1, 10))
    }

    @Test
    fun `last place is top hundred percent`() {
        assertEquals(100, topPercentOf(10, 10))
    }

    @Test
    fun `percentile rounds to the nearest whole percent`() {
        assertEquals(33, topPercentOf(1, 3))
        assertEquals(67, topPercentOf(2, 3))
    }

    @Test
    fun `ranked learners never show zero percent`() {
        assertEquals(1, topPercentOf(1, 1000))
    }
}
