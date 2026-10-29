package com.nexus.audit.cli

import com.nexus.audit.analyzers.PhaseAnalyzerRegistry
import com.nexus.audit.core.AuditOrchestrator
import com.nexus.audit.core.types.*
import com.nexus.audit.reporting.AuditReporter
import kotlinx.coroutines.runBlocking
import java.io.File

/**
 * Command-line entry point for the Nexus production-readiness audit.
 *
 * Usage:
 *   java -jar audit-cli.jar --root <projectRoot> --out <reportDir> [--format md|json|html]
 *
 * Exit code is non-zero when the audit produces a NOT_READY recommendation,
 * so it can gate CI pipelines.
 */
fun main(args: Array<String>) {
    val parsed = args.toList().chunked(2).mapNotNull { (k, v) -> k to v }.toMap()
    val root = File(parsed["--root"] ?: System.getProperty("user.dir"))
    val out = File(parsed["--out"] ?: "build/audit")
    val formats = (parsed["--format"] ?: "md")
        .split(",").mapNotNull {
            when (it.trim().lowercase()) {
                "md", "markdown" -> OutputFormat.MARKDOWN
                "json" -> OutputFormat.JSON
                "html" -> OutputFormat.HTML
                else -> null
            }
        }.toSet().ifEmpty { setOf(OutputFormat.MARKDOWN) }

    require(root.exists() && root.isDirectory) { "Project root not found: $root" }

    val config = AuditConfig(
        projectRoot = root,
        enabledPhases = (1..8).toSet(),
        reportOutputDir = out,
        outputFormat = formats
    )

    println("Nexus Audit → scanning ${root.absolutePath}")
    val orchestrator = AuditOrchestrator(PhaseAnalyzerRegistry.all())
    val result = runBlocking { orchestrator.runFullAudit(config) }

    val files = AuditReporter.writeReports(config, result)
    println("Deployment recommendation: ${result.deploymentDecision.recommendation}")
    println("Launch readiness: ${result.launchReadinessScore}/10 | Startup quality: ${result.startupQualityScore}/10")
    println("Reports written to: ${out.absolutePath}")
    files.forEach { println("  - ${it.name}") }

    if (result.deploymentDecision.recommendation == DeploymentRecommendation.NOT_READY) {
        System.exit(2)
    }
}
