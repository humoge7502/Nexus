package com.nexus.audit.analyzers

import com.nexus.audit.core.analyzer.PhaseAnalyzer

/**
 * Builds the full set of phase analyzers keyed by phase number (1-8) for the
 * [com.nexus.audit.core.AuditOrchestrator].
 */
object PhaseAnalyzerRegistry {
    fun all(): Map<Int, PhaseAnalyzer> = mapOf(
        1 to RealDataAnalyzer(),
        2 to ApiIntegrationAnalyzer(),
        3 to ArchitectureAnalyzer(),
        4 to UiUxAnalyzer(),
        5 to PerformanceAnalyzer(),
        6 to SecurityAnalyzer(),
        7 to DeploymentAnalyzer(),
        8 to StartupQualityAnalyzer()
    )

    fun byPhases(phases: Set<Int>): Map<Int, PhaseAnalyzer> =
        all().filterKeys { it in phases }
}
