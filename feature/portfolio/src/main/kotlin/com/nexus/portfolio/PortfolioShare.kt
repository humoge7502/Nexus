package com.nexus.portfolio

import com.nexus.domain.model.PortfolioProject

/**
 * Plain-text rendering of the portfolio, shaped for the places it actually gets
 * pasted: messaging apps, email and recruiters' notes. Only fields the user has
 * filled are emitted, so the export never carries a placeholder.
 */
internal fun portfolioShareText(projects: List<PortfolioProject>): String = buildString {
    appendLine("My projects")
    appendLine()
    projects.forEachIndexed { index, project ->
        appendLine("${index + 1}. ${project.title}")
        project.description.takeIf { it.isNotBlank() }?.let { appendLine("   $it") }
        project.techStack.takeIf { it.isNotEmpty() }
            ?.let { appendLine("   Tech: ${it.joinToString(", ")}") }
        project.githubUrl?.takeIf { it.isNotBlank() }?.let { appendLine("   Code: $it") }
        project.liveUrl?.takeIf { it.isNotBlank() }?.let { appendLine("   Live: $it") }
        if (index != projects.lastIndex) appendLine()
    }
}.trimEnd()
