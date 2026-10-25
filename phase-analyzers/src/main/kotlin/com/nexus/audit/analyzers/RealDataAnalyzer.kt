package com.nexus.audit.analyzers

import com.nexus.audit.core.analyzer.PhaseAnalyzer
import com.nexus.audit.core.types.*

/**
 * Phase 1 — Real Data Verification.
 *
 * Detects mock / demo / placeholder data leaking into production builds and
 * confirms that genuine datasets (assets / ml / csv / json) back the experience.
 */
class RealDataAnalyzer : PhaseAnalyzer {
    override val phaseNumber = 1
    override val phaseName = "Real Data Verification"

    private val mockPatterns = listOf(
        "MockData object" to """(object|class)\s+Mock[A-Za-z]*|val\s+mock[A-Za-z]*\s*[:=]""",
        "Fake generator" to """Faker\s*\(|fake\s*\(|FakeDataGenerator|dummyData|placeholderData|fakeResponse|mockResponse""",
        "Demo flag" to """DEMO_MODE|isDemo\s*=\s*true|"demo"|"sample"|sampleResponse"""
    )

    override suspend fun analyze(context: AuditContext): PhaseResult {
        val files = ScanUtil.productionFiles(context.sourceFiles)
        val prod = files.filterNot { ScanUtil.isTestFile(it) }
        val findings = mutableListOf<Finding>()

        for ((label, pattern) in mockPatterns) {
            val hits = ScanUtil.grep(prod, Regex(pattern))
            if (hits.isNotEmpty()) {
                val sample = hits.first()
                findings.add(
                    ScanUtil.finding(
                        severity = Severity.MEDIUM,
                        category = "Real Data",
                        title = "Possible mock/demo data in production ($label)",
                        description = "Found ${hits.size} match(es) for '$label' outside test sources. " +
                            "Shipping mock data erodes user trust and pollutes analytics.",
                        recommendation = "Source this from the real backend, or gate demo mode behind a remote " +
                            "config flag that is OFF by default in release builds.",
                        hit = sample,
                        requirement = "DATA-001"
                    )
                )
            }
        }

        val root = context.projectRoot
        val dataDirs = listOf("ml", "assets", "datasets", "data").map { java.io.File(root, it) }
        val hasDataDir = dataDirs.any { it.exists() && (it.listFiles()?.isNotEmpty() == true) }
        val dataFiles = files.filter {
            (it.extension == "csv" || it.extension == "json") && !ScanUtil.isTestFile(it)
        }
        if (hasDataDir || dataFiles.isNotEmpty()) {
            findings.add(
                ScanUtil.finding(
                    severity = Severity.INFO,
                    category = "Real Data",
                    title = "Real datasets present",
                    description = "Found dataset directories/assets (${dataFiles.size} csv/json data files under " +
                        "source trees). This supports a data-driven experience.",
                    recommendation = "Add a data dictionary and document provenance; keep datasets versioned and " +
                        "reproducibly downloadable in CI.",
                    requirement = "DATA-002"
                )
            )
        }

        if (findings.none { it.severity != Severity.INFO }) {
            findings.add(
                ScanUtil.finding(
                    severity = Severity.INFO,
                    category = "Real Data",
                    title = "No mock-data leaks detected",
                    description = "Static scan found no mock/demo data patterns in production sources.",
                    recommendation = "Add a CI guard that fails the build if 'MockData' / 'DEMO_MODE' appears " +
                        "outside test sources.",
                    requirement = "DATA-001"
                )
            )
        }

        return GenericPhaseResult(
            phaseNumber = phaseNumber,
            findings = findings,
            metadata = mapOf(
                "filesScanned" to files.size.toString(),
                "dataFiles" to dataFiles.size.toString()
            )
        )
    }

    override fun calculateScore(result: PhaseResult): PhaseScore {
        val med = result.findings.count { it.severity == Severity.MEDIUM }
        val high = result.findings.count { it.severity == Severity.HIGH }
        val score = (10.0 - high * 3.0 - med * 1.5).coerceIn(1.0, 10.0)
        return PhaseScore(phaseNumber, phaseName, score,
            "Detected $med medium and $high high data-integrity findings.")
    }
}
