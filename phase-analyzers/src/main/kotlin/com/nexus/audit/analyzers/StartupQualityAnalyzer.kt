package com.nexus.audit.analyzers

import com.nexus.audit.core.analyzer.PhaseAnalyzer
import com.nexus.audit.core.types.*
import java.io.File

/**
 * Phase 8 — Startup Quality.
 *
 * Inspects Application-class initialization for heavy main-thread work that
 * delays first frame, and confirms DI/startup wiring.
 */
class StartupQualityAnalyzer : PhaseAnalyzer {
    override val phaseNumber = 8
    override val phaseName = "Startup Quality"

    override suspend fun analyze(context: AuditContext): PhaseResult {
        val files = ScanUtil.productionFiles(context.sourceFiles)
        val findings = mutableListOf<Finding>()

        val appClasses = files.filter {
            ScanUtil.readQuietly(it).contains("class") && ScanUtil.readQuietly(it)
                .contains(Regex("""(:\s*Application|extends Application|Application\(\))\b"""))
        }

        for (ac in appClasses) {
            val text = ScanUtil.readQuietly(ac)
            // Heavy work in onCreate
            val onCreateIdx = text.indexOf("onCreate")
            val tail = if (onCreateIdx >= 0) text.drop(onCreateIdx).take(2000) else text
            val heavy = listOf("runBlocking", "Thread.sleep", "File(", "SQLite", "URL(", "HttpURLConnection",
                "createDatabase", "Room.databaseBuilder", "StrictMode")
            val offenders = heavy.filter { tail.contains(it) }
            if (offenders.isNotEmpty()) {
                findings.add(
                    ScanUtil.finding(
                        severity = Severity.HIGH,
                        category = "Startup",
                        title = "Heavy work on Application startup path",
                        description = "Application (${ac.name}) performs potentially blocking work at startup: " +
                            offenders.joinToString() + ". This delays first frame and hurts cold-start metrics.",
                        recommendation = "Defer heavy init with App Startup / coroutines on Dispatchers.IO; " +
                            "use lazy DI graphs; prefetch on a background thread.",
                        hit = ScanUtil.Hit(ac, 1, offenders.joinToString()),
                        requirement = "STARTUP-001"
                    )
                )
            }
            if (!text.contains("StrictMode") && offenders.isEmpty()) {
                findings.add(
                    ScanUtil.finding(
                        severity = Severity.LOW,
                        category = "Startup",
                        title = "No StrictMode configuration in Application",
                        description = "Application (${ac.name}) does not enable StrictMode, so accidental disk/network " +
                            "access on the main thread goes unnoticed.",
                        recommendation = "Enable StrictMode (thread + vm policy) in debug builds to catch regressions.",
                        hit = ScanUtil.Hit(ac, 1, ac.name),
                        requirement = "STARTUP-002"
                    )
                )
            }
        }

        if (appClasses.isEmpty()) {
            findings.add(
                ScanUtil.finding(
                    severity = Severity.INFO,
                    category = "Startup",
                    title = "No custom Application class detected",
                    description = "No Application subclass found; default Android startup applies. This is fine if " +
                        "initialization is handled via Hilt/App Startup.",
                    recommendation = "If adding heavy init, prefer androidx.startup.Initializer over Application.onCreate.",
                    requirement = "STARTUP-001"
                )
            )
        }

        return GenericPhaseResult(
            phaseNumber = phaseNumber,
            findings = findings,
            metadata = mapOf("appClasses" to appClasses.size.toString())
        )
    }

    override fun calculateScore(result: PhaseResult): PhaseScore {
        val high = result.findings.count { it.severity == Severity.HIGH }
        val med = result.findings.count { it.severity == Severity.MEDIUM }
        val low = result.findings.count { it.severity == Severity.LOW }
        val score = (10.0 - high * 3.0 - med * 1.5 - low * 0.5).coerceIn(1.0, 10.0)
        return PhaseScore(phaseNumber, phaseName, score,
            "Startup: $high high / $med medium / $low low findings.")
    }
}
