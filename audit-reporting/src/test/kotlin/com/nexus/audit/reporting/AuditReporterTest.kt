package com.nexus.audit.reporting

import com.nexus.audit.core.types.AuditResult
import com.nexus.audit.core.types.CodeLocation
import com.nexus.audit.core.types.Complexity
import com.nexus.audit.core.types.DeploymentDecision
import com.nexus.audit.core.types.DeploymentRecommendation
import com.nexus.audit.core.types.EstimatedEffort
import com.nexus.audit.core.types.Finding
import com.nexus.audit.core.types.GenericPhaseResult
import com.nexus.audit.core.types.PhaseScore
import com.nexus.audit.core.types.Severity
import org.junit.Assert.assertTrue
import org.junit.Test

class AuditReporterTest {

    private fun sampleResult(): AuditResult {
        val finding = Finding(
            severity = Severity.HIGH,
            category = "Security",
            title = "Plaintext endpoint",
            description = "An http:// endpoint was found.",
            location = null,
            recommendation = "Use https://."
        )
        val phase = GenericPhaseResult(1, listOf(finding))
        val score = PhaseScore(1, "Real Data Verification", 8.0, "ok")
        val decision = DeploymentDecision(
            recommendation = DeploymentRecommendation.READY_WITH_RISKS,
            reasoning = "One high-risk item.",
            topPriorities = listOf("Fix endpoint"),
            estimatedEffort = EstimatedEffort(1.0, Complexity.SIMPLE),
            blockers = emptyList(),
            highRiskSystems = listOf("Plaintext endpoint")
        )
        return AuditResult(
            phaseResults = mapOf(1 to phase),
            phaseScores = mapOf(1 to score),
            launchReadinessScore = 8.0,
            startupQualityScore = 8.5,
            deploymentDecision = decision,
            executionTime = "PT1S",
            timestamp = "2026-10-27T00:00:00Z"
        )
    }

    @Test
    fun `markdown contains decision scores and findings`() {
        val md = AuditReporter.renderMarkdown(sampleResult())
        assertTrue(md.contains("Nexus Production-Readiness Audit"))
        assertTrue(md.contains("READY_WITH_RISKS"))
        assertTrue(md.contains("Plaintext endpoint"))
        assertTrue(md.contains("8.00/10"))
    }

    @Test
    fun `locations render relative to the project root`() {
        val root = java.nio.file.Files.createTempDirectory("audit-reporter").toFile()
        val finding = Finding(
            severity = Severity.HIGH,
            category = "Security",
            title = "Unreliable host",
            description = "A community-hosted API is referenced.",
            location = CodeLocation(java.io.File(root, "core/network/build.gradle.kts"), line = 30),
            recommendation = "Add a fallback source."
        )
        val result = sampleResult().copy(
            phaseResults = mapOf(1 to GenericPhaseResult(1, listOf(finding))),
        )

        val md = AuditReporter.renderMarkdown(result, root).replace('\\', '/')
        assertTrue(md.contains("_Location: core/network/build.gradle.kts:30_"))

        val html = AuditReporter.renderHtml(result, root).replace('\\', '/')
        assertTrue(html.contains("core/network/build.gradle.kts:30"))
    }

    @Test
    fun `json is machine readable`() {
        val json = AuditReporter.renderJson(sampleResult())
        assertTrue(json.contains("\"launchReadinessScore\""))
        assertTrue(json.contains("\"READY_WITH_RISKS\""))
        assertTrue(json.contains("Plaintext endpoint"))
    }

    @Test
    fun `html is a complete document`() {
        val html = AuditReporter.renderHtml(sampleResult())
        assertTrue(html.startsWith("<!doctype html>"))
        assertTrue(html.contains("READY_WITH_RISKS"))
        assertTrue(html.contains("Plaintext endpoint"))
    }
}
