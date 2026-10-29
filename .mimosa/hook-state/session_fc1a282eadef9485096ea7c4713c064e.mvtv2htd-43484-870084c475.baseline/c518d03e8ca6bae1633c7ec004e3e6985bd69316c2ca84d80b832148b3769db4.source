package com.nexus.domain.usecase

import com.nexus.datastore.UserPreferences
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ScorePulseGeneratorTest {

    private val generator = ScorePulseGenerator(mockk<UserPreferences>(relaxed = true))

    @Test
    fun `no pulse is emitted when the score does not increase`() {
        assertNull(generator.generatePulse(100, 100, "dsa_problem", ""))
        assertNull(generator.generatePulse(100, 90, "dsa_problem", ""))
    }

    @Test
    fun `pulse carries the score delta and trigger metadata`() {
        val pulse = requireNotNull(generator.generatePulse(40, 80, "dsa_problem", "Two Sum"))
        assertEquals(40, pulse.delta)
        assertEquals(40, pulse.oldScore)
        assertEquals(80, pulse.newScore)
        assertEquals("dsa_problem", pulse.triggerSource)
        assertEquals("Two Sum", pulse.triggerDetail)
    }

    @Test
    fun `dsa problems point at the next milestone`() {
        assertEquals(
            "20 more points → score 100",
            generator.generatePulse(60, 80, "dsa_problem", "")?.nextMilestone,
        )
        assertEquals(
            "100 more points → score 300",
            generator.generatePulse(150, 200, "dsa_problem", "")?.nextMilestone,
        )
    }

    @Test
    fun `trigger-specific nudges are used for careers and interviews`() {
        assertEquals(
            "Daily hit tomorrow → maintain momentum",
            generator.generatePulse(10, 20, "career_hit", "")?.nextMilestone,
        )
        assertEquals(
            "Log interview feedback → unlock insights",
            generator.generatePulse(10, 20, "interview_logged", "")?.nextMilestone,
        )
    }

    @Test
    fun `unknown triggers fall back to the milestone message`() {
        assertEquals(
            "100 points to score 300",
            generator.generatePulse(100, 200, "manual", "")?.nextMilestone,
        )
    }

    @Test
    fun `scores above the last milestone cap at one thousand`() {
        assertEquals(
            "0 points to score 1000",
            generator.generatePulse(900, 1000, "manual", "")?.nextMilestone,
        )
        assertEquals(
            "-50 points to score 1000",
            generator.generatePulse(1000, 1050, "manual", "")?.nextMilestone,
        )
    }
}
