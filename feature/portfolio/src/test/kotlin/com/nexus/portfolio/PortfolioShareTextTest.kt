package com.nexus.portfolio

import com.nexus.domain.model.PortfolioProject
import kotlinx.collections.immutable.persistentListOf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PortfolioShareTextTest {

    private fun project(
        title: String,
        description: String = "",
        tech: List<String> = emptyList(),
        githubUrl: String? = null,
        liveUrl: String? = null,
    ) = PortfolioProject(
        id = title,
        title = title,
        description = description,
        techStack = persistentListOf(*tech.toTypedArray()),
        githubUrl = githubUrl,
        liveUrl = liveUrl,
    )

    @Test
    fun `numbers every project in order`() {
        val text = portfolioShareText(
            listOf(project("Alpha"), project("Beta"))
        )
        assertTrue(text.contains("1. Alpha"))
        assertTrue(text.contains("2. Beta"))
    }

    @Test
    fun `omits fields the user left empty`() {
        val text = portfolioShareText(listOf(project("Alpha")))
        assertFalse(text.contains("Tech:"))
        assertFalse(text.contains("Code:"))
        assertFalse(text.contains("Live:"))
    }

    @Test
    fun `keeps a description that spans several lines intact`() {
        val text = portfolioShareText(
            listOf(project("Alpha", description = "One\nTwo"))
        )
        assertTrue(text.contains("One"))
        assertTrue(text.contains("Two"))
    }

    @Test
    fun `renders a single project with no trailing blank line`() {
        val text = portfolioShareText(listOf(project("Alpha")))
        assertFalse(text.endsWith("\n"))
        assertEquals("My projects\n\n1. Alpha", text)
    }

    @Test
    fun `joins the tech stack with a readable separator`() {
        val text = portfolioShareText(
            listOf(project("Alpha", tech = listOf("Kotlin", "Compose")))
        )
        assertTrue(text.contains("Tech: Kotlin, Compose"))
    }
}
