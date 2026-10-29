package com.nexus.domain.usecase

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FeatureGateManagerTest {

    private val manager = FeatureGateManager()

    @Test
    fun `nothing unlocks before onboarding day one`() {
        assertTrue(manager.getUnlockedGates(0).isEmpty())
    }

    @Test
    fun `day one unlocks the core five features`() {
        assertEquals(
            setOf(
                FeatureGate.BASIC_CHAT,
                FeatureGate.DSA_TRACKER,
                FeatureGate.CAREER_HITS,
                FeatureGate.STREAK_TRACKING,
                FeatureGate.OPPORTUNITIES,
            ),
            manager.getUnlockedGates(1),
        )
    }

    @Test
    fun `week one adds roadmap certifications and skill gap`() {
        val gates = manager.getUnlockedGates(7)
        assertEquals(8, gates.size)
        assertTrue(gates.containsAll(setOf(FeatureGate.ROADMAP, FeatureGate.CERTIFICATIONS, FeatureGate.SKILL_GAP)))
    }

    @Test
    fun `two weeks adds interview coach linkedin and leaderboard`() {
        val gates = manager.getUnlockedGates(14)
        assertEquals(11, gates.size)
        assertTrue(gates.containsAll(setOf(
            FeatureGate.INTERVIEW_COACH,
            FeatureGate.LINKEDIN_POST,
            FeatureGate.LEADERBOARD,
        )))
    }

    @Test
    fun `thirty days unlocks the full feature set`() {
        assertEquals(FeatureGate.entries.toSet(), manager.getUnlockedGates(30))
        assertEquals(14, FeatureGate.entries.size)
    }

    @Test
    fun `isFeatureUnlocked respects the exact boundary`() {
        assertFalse(manager.isFeatureUnlocked(FeatureGate.ORACLE, 29))
        assertTrue(manager.isFeatureUnlocked(FeatureGate.ORACLE, 30))
        assertFalse(manager.isFeatureUnlocked(FeatureGate.BASIC_CHAT, 0))
        assertTrue(manager.isFeatureUnlocked(FeatureGate.BASIC_CHAT, 1))
    }

    @Test
    fun `future days never lose unlocked gates`() {
        assertTrue(manager.getUnlockedGates(365).containsAll(manager.getUnlockedGates(30)))
    }
}
