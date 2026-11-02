package com.nexus.domain.usecase

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class NewMatchPlannerTest {

    private val today = LocalDate.of(2026, 11, 2)

    private fun candidate(
        id: String,
        matched: List<String>,
        deadline: LocalDate? = null,
        title: String = "Role $id",
        kind: NewMatchKind = NewMatchKind.INTERNSHIP,
    ) = NewMatchCandidate(
        kind = kind,
        id = id,
        title = title,
        org = "TestCo",
        matchedSkills = matched,
        deadline = deadline,
    )

    @Test
    fun `skill comparison survives case and spacing differences between sources`() {
        val matched = NewMatchPlanner.matchedSkills(
            required = listOf(" Node.js ", "React", "GraphQL", "react"),
            ownedSkills = setOf("node.js", "REACT"),
        )
        assertEquals(listOf("Node.js", "React"), matched)
    }

    @Test
    fun `a student with no skills recorded matches nothing`() {
        assertTrue(
            NewMatchPlanner.matchedSkills(listOf("Python"), emptySet()).isEmpty(),
        )
    }

    @Test
    fun `an item that asks for nothing the student has is never announced`() {
        val silent = NewMatchPlanner.select(
            listOf(candidate("a", emptyList(), today.plusDays(2))),
            today,
        )
        assertNull(silent)
    }

    @Test
    fun `the broadest match wins over a closer deadline`() {
        val chosen = NewMatchPlanner.select(
            listOf(
                candidate("tight", listOf("Python"), today.plusDays(1)),
                candidate("broad", listOf("Python", "SQL", "React"), today.plusDays(30)),
            ),
            today,
        )
        assertEquals("broad", chosen?.id)
    }

    @Test
    fun `equal coverage prefers the listing that closes first`() {
        val chosen = NewMatchPlanner.select(
            listOf(
                candidate("rolling", listOf("Python"), null),
                candidate("closing", listOf("Python"), today.plusDays(4)),
            ),
            today,
        )
        assertEquals("closing", chosen?.id)
    }

    @Test
    fun `a rolling listing is still announceable because it has no date to miss`() {
        val chosen = NewMatchPlanner.select(
            listOf(candidate("rolling", listOf("Python"), null)),
            today,
        )
        assertEquals("rolling", chosen?.id)
    }

    @Test
    fun `an already closed listing is dropped`() {
        val chosen = NewMatchPlanner.select(
            listOf(candidate("stale", listOf("Python"), today.minusDays(1))),
            today,
        )
        assertNull(chosen)
    }

    @Test
    fun `keys separate kinds so the same id cannot mute an alert`() {
        val internship = candidate("42", listOf("Python"))
        val hackathon = candidate(
            "42",
            listOf("Python"),
            kind = NewMatchKind.HACKATHON,
        )
        assertEquals("INTERNSHIP:42", internship.key())
        assertEquals("HACKATHON:42", hackathon.key())
        assertFalse(internship.key() == hackathon.key())
    }

    @Test
    fun `a listing written differently still reaches the student who claimed it`() {
        val matched = NewMatchPlanner.matchedSkills(
            required = listOf("ReactJS", "Machine Learning (ML)", "JavaScript"),
            ownedSkills = setOf("React", "ML"),
        )
        assertEquals(listOf("ReactJS", "Machine Learning (ML)"), matched)
    }
}
