package com.nexus.designsystem.component

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Pins the rounding contract of [formatSweep]: the string the user reads
 * and the swept float the gauge arc reads are derived from the identical
 * value, never from two clocks (Port 1, `ui-kit/COMPOSE_PORTS.md`).
 */
class SweepFormatTest {

    @Test
    fun `one decimal by default`() {
        assertEquals("72.4", formatSweep(72.4f))
    }

    @Test
    fun `zero decimals renders an integer`() {
        assertEquals("72", formatSweep(72f, decimals = 0))
    }

    @Test
    fun `zero decimals rounds half up`() {
        assertEquals("73", formatSweep(72.6f, decimals = 0))
    }

    @Test
    fun `unit suffix is appended without spacing`() {
        assertEquals("72%", formatSweep(72f, decimals = 0, unit = "%"))
        assertEquals("72.4%", formatSweep(72.4f, unit = "%"))
    }

    @Test
    fun `null unit formats bare digits`() {
        assertEquals("72.4", formatSweep(72.4f, unit = null))
    }

    @Test
    fun `negative zero never renders a sign`() {
        assertEquals("0.0", formatSweep(-0.01f, decimals = 1))
    }

    @Test
    fun `decimals are coerced to a sane range`() {
        assertEquals("72", formatSweep(72.4f, decimals = -3))
    }

    @Test
    fun `int helper rounds half up`() {
        assertEquals("73", formatSweepInt(72.6f))
        assertEquals("72", formatSweepInt(72.4f))
    }

    @Test
    fun `output is stable across locales`() {
        // The pattern pins Locale.US, so a comma-decimal default locale
        // cannot change the readout shape.
        assertEquals("72.4", formatSweep(72.4f, decimals = 1))
    }
}
