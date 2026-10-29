package com.nexus.data.repository

import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class InternshipDeadlineMapperTest {

    @Test
    fun `epoch millis seed value maps to its UTC date`() {
        // 2025-07-01T00:00:00Z — value used by opportunities_seed.json
        assertEquals(LocalDate.of(2025, 7, 1), 1751328000000L.toDeadlineDate())
    }

    @Test
    fun `epoch days value maps unchanged`() {
        val date = LocalDate.of(2026, 1, 15)
        assertEquals(date, date.toEpochDay().toDeadlineDate())
    }

    @Test
    fun `zero and missing deadlines do not throw`() {
        assertEquals(LocalDate.ofEpochDay(0), 0L.toDeadlineDate())
    }
}
