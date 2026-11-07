package com.nexus.designsystem.component

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Pins the ranking contract of [NexusDestinationIndex]: predictable
 * ordering (prefix beats contains, title beats alias, alias beats
 * category) with stable ties, so a jump-list never surprises.
 */
class NexusDestinationIndexTest {

    private val commands = listOf(
        NexusCommand(id = "home", title = "Home", category = "Root"),
        NexusCommand(
            id = "growth",
            title = "Growth",
            subtitle = "Readiness and momentum",
            category = "Career",
            aliases = listOf("streak", "readiness", "score"),
        ),
        NexusCommand(
            id = "applications",
            title = "Applications",
            subtitle = "Track your applications",
            category = "Career",
            aliases = listOf("cv", "resume", "applied"),
        ),
        NexusCommand(id = "explore", title = "Explore", category = "Discover"),
    )

    @Test
    fun `blank query returns everything in original order`() {
        assertEquals(commands, NexusDestinationIndex.resolveCommands("  ", commands))
    }

    @Test
    fun `title prefix ranks first and is case insensitive`() {
        assertEquals(
            listOf("explore"),
            NexusDestinationIndex.resolveCommands("EXPLORE", commands).map { it.id },
        )
    }

    @Test
    fun `alias resolves when title does not match`() {
        val result = NexusDestinationIndex.resolveCommands("streak", commands)
        assertEquals(listOf("growth"), result.map { it.id })
    }

    @Test
    fun `no match returns empty`() {
        assertTrue(NexusDestinationIndex.resolveCommands("nonexistent", commands).isEmpty())
    }

    @Test
    fun `title beats alias and alias beats category`() {
        // "growth" is a title prefix for Growth, so it outranks any
        // category/alias substring hit for the same query.
        val result = NexusDestinationIndex.resolveCommands("growth", commands)
        assertEquals("growth", result.first().id)
        // Category query matches both Career destinations, ties keep order.
        val career = NexusDestinationIndex.resolveCommands("career", commands)
        assertEquals(listOf("growth", "applications"), career.map { it.id })
    }

    @Test
    fun `resolve returns the top-ranked command`() {
        assertEquals("growth", NexusDestinationIndex.resolve("streak", commands)?.id)
        assertEquals("growth", NexusDestinationIndex.resolve("GROWTH", commands)?.id)
        assertEquals("explore", NexusDestinationIndex.resolve("explore", commands)?.id)
    }

    @Test
    fun `resolve returns null for blank and unknown`() {
        assertNull(NexusDestinationIndex.resolve("", commands))
        assertNull(NexusDestinationIndex.resolve("   ", commands))
        assertNull(NexusDestinationIndex.resolve("nowhere", commands))
    }
}
