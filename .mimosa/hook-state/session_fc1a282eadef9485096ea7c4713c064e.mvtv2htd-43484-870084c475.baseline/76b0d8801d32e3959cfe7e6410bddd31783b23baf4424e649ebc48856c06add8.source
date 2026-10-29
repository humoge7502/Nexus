package com.nexus.portfolio

import com.nexus.domain.model.PortfolioProject
import org.junit.Assert.assertEquals
import org.junit.Test

class PortfolioUrlTest {

    @Test
    fun `adds a scheme to the host path people actually type`() {
        assertEquals("https://github.com/nexus/resume-copilot", normalizePortfolioUrl("github.com/nexus/resume-copilot"))
    }

    @Test
    fun `leaves a url that already carries its scheme alone`() {
        assertEquals("http://localhost:3000", normalizePortfolioUrl("http://localhost:3000"))
        assertEquals("https://demo.app", normalizePortfolioUrl("https://demo.app"))
    }

    @Test
    fun `shares the code link beside the project it belongs to`() {
        val project = PortfolioProject(
            id = "alpha",
            title = "Alpha",
            githubUrl = "github.com/nexus/alpha",
        )
        val text = portfolioShareText(listOf(project))
        assertEquals("My projects\n\n1. Alpha\n   Code: github.com/nexus/alpha", text)
    }
}
