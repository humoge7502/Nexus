package com.nexus.audit.analyzers

import com.nexus.audit.core.analyzer.PhaseAnalyzer
import com.nexus.audit.core.types.*
import com.nexus.audit.rules.ArchitectureRules
import com.nexus.audit.rules.RuleCatalog

/**
 * Phase 3 — Architecture & Layering.
 *
 * Enforces modular layering (no feature→feature deps), flags oversized files,
 * and detects main-thread blocking via runBlocking.
 */
class ArchitectureAnalyzer : PhaseAnalyzer {
    override val phaseNumber = 3
    override val phaseName = "Architecture & Layering"

    override suspend fun analyze(context: AuditContext): PhaseResult {
        val files = ScanUtil.productionSources(context.sourceFiles)
        val findings = mutableListOf<Finding>()

        // 1) Feature→feature dependency detection from build files
        val buildFiles = context.buildFiles.filter {
            !it.absolutePath.contains("/build/") && !it.absolutePath.contains("\\build\\")
        }
        val projectDep = Regex("""project\(\s*["']([^"']+)["']\s*\)""")
        for (build in buildFiles) {
            val text = ScanUtil.readQuietly(build)
            val moduleName = build.parentFile.name
            // Only modules that themselves live under feature/ can violate the rule;
            // app -> feature wiring is the intended composition root.
            val declaredInFeature = build.parentFile.parentFile?.name == "feature"
            if (!declaredInFeature) continue
            projectDep.findAll(text).forEach { m ->
                val dep = m.groupValues[1].removePrefix(":")
                if (dep.startsWith("feature") && dep != moduleName) {
                    findings.add(
                        ScanUtil.finding(
                            severity = Severity.HIGH,
                            category = "Architecture",
                            title = "Feature module depends on another feature module",
                            description = "Module '$moduleName' declares project(\":$dep\"). This violates clean " +
                                "modular layering and creates a fragile dependency graph.",
                            recommendation = ArchitectureRules.LAYERING_VIOLATION.remediation +
                                " Rule ${RuleCatalog["ARCH-001"].id}.",
                            hit = ScanUtil.Hit(build, 0, "project(\":$dep\")"),
                            requirement = "ARCH-001"
                        )
                    )
                }
            }
        }

        // 2) Oversized files
        val oversized = files.filter { ScanUtil.lineCount(it) > 600 }
        oversized.sortedByDescending { ScanUtil.lineCount(it) }.take(15).forEach { f ->
            findings.add(
                ScanUtil.finding(
                    severity = Severity.MEDIUM,
                    category = "Architecture",
                    title = "Oversized source file",
                    description = "'${ScanUtil.relativePath(f, context.projectRoot)}' has ${ScanUtil.lineCount(f)} lines, " +
                        "above the 600-line maintainability threshold.",
                    recommendation = ArchitectureRules.OVERSIZED_FILE.remediation +
                        " Rule ${RuleCatalog["ARCH-002"].id}.",
                    hit = ScanUtil.Hit(f, 1, "${ScanUtil.lineCount(f)} lines"),
                    requirement = "ARCH-002"
                )
            )
        }

        // 3) runBlocking in production (audit-allow comments mark contract-mandated uses)
        val rb = ScanUtil.grep(ScanUtil.productionSources(context.sourceFiles), Regex("""runBlocking\s*\{"""))
            .filterNot { it.text.contains("audit-allow: ARCH-003") }
        rb.forEach { hit ->
            findings.add(
                ScanUtil.finding(
                    severity = Severity.HIGH,
                    category = "Architecture",
                    title = "runBlocking on a production code path",
                    description = "runBlocking blocks the calling thread; on a dispatcher/main thread it causes " +
                        "jank or ANR. Found in ${hit.file.name}:${hit.line}.",
                    recommendation = ArchitectureRules.MAIN_THREAD_BLOCKING.remediation +
                        " Rule ${RuleCatalog["ARCH-003"].id}.",
                    hit = hit,
                    requirement = "ARCH-003"
                )
            )
        }

        if (findings.isEmpty()) {
            findings.add(
                ScanUtil.finding(
                    severity = Severity.INFO,
                    category = "Architecture",
                    title = "Layering and size checks passed",
                    description = "No feature→feature dependencies, oversized files, or runBlocking sites detected.",
                    recommendation = "Keep the module graph acyclic; add a Gradle convention plugin to enforce it.",
                    requirement = "ARCH-001"
                )
            )
        }

        return GenericPhaseResult(
            phaseNumber = phaseNumber,
            findings = findings,
            metadata = mapOf(
                "oversizedFiles" to oversized.size.toString(),
                "runBlocking" to rb.size.toString()
            )
        )
    }

    override fun calculateScore(result: PhaseResult): PhaseScore {
        val high = result.findings.count { it.severity == Severity.HIGH }
        val med = result.findings.count { it.severity == Severity.MEDIUM }
        val score = (10.0 - high * 3.0 - med * 1.0).coerceIn(1.0, 10.0)
        return PhaseScore(phaseNumber, phaseName, score,
            "Architecture: $high high / $med medium findings.")
    }
}
