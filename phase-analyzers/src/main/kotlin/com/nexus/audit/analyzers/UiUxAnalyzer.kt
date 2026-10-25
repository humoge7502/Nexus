package com.nexus.audit.analyzers

import com.nexus.audit.core.analyzer.PhaseAnalyzer
import com.nexus.audit.core.types.*

/**
 * Phase 4 — UI/UX Quality.
 *
 * Checks accessibility (contentDescription), Material 3 theming, dark-mode
 * support, and string hardcoding.
 */
class UiUxAnalyzer : PhaseAnalyzer {
    override val phaseNumber = 4
    override val phaseName = "UI/UX Quality"

    override suspend fun analyze(context: AuditContext): PhaseResult {
        val files = ScanUtil.productionFiles(context.sourceFiles)
            .filter { it.extension == "kt" }
        val findings = mutableListOf<Finding>()

        // Compose Image without contentDescription
        val imageNoDesc = ScanUtil.grep(files.filterNot { ScanUtil.isTestFile(it) },
            Regex("""Image\s*\(\s*(painter|imageVector|bitmap)\s*=.*\)"""))
            .filter { hit -> !ScanUtil.readQuietly(hit.file).lineSequence()
                .drop(hit.line - 1).take(6)
                .any { it.contains("contentDescription") } }
        if (imageNoDesc.isNotEmpty()) {
            findings.add(
                ScanUtil.finding(
                    severity = Severity.MEDIUM,
                    category = "UI/UX",
                    title = "Image missing contentDescription (accessibility)",
                    description = "Found ${imageNoDesc.size} Compose Image call(s) without a contentDescription, " +
                        "breaking screen-reader support.",
                    recommendation = "Add contentDescription = if (decorative) null else stringResource(...).",
                    hit = imageNoDesc.first(),
                    requirement = "UIUX-001"
                )
            )
        }

        // Hardcoded strings in composables
        val hardcoded = ScanUtil.grep(files.filterNot { ScanUtil.isTestFile(it) },
            Regex("""Text\s*\(\s*"[^"]{3,}"""))
        if (hardcoded.size > 20) {
            findings.add(
                ScanUtil.finding(
                    severity = Severity.LOW,
                    category = "UI/UX",
                    title = "Large number of hardcoded strings",
                    description = "Detected ${hardcoded.size} Text(\"...\") literals. Hardcoding UI copy hurts " +
                        "localization and consistency.",
                    recommendation = "Move copy to string resources and reference via stringResource().",
                    requirement = "UIUX-002"
                )
            )
        }

        // Theming check
        val hasTheme = ScanUtil.grep(files, Regex("""MaterialTheme|NexusTheme|dynamicColor|isSystemInDarkTheme"))
        if (hasTheme.isEmpty()) {
            findings.add(
                ScanUtil.finding(
                    severity = Severity.LOW,
                    category = "UI/UX",
                    title = "No Material/theme definition detected",
                    description = "No MaterialTheme / dark-mode handling found. Users may get an inconsistent look.",
                    recommendation = "Adopt Material 3 theming with light/dark color schemes.",
                    requirement = "UIUX-003"
                )
            )
        } else {
            findings.add(
                ScanUtil.finding(
                    severity = Severity.INFO,
                    category = "UI/UX",
                    title = "Theming present",
                    description = "Material/dynamic theming and dark-mode handling detected in sources.",
                    recommendation = "Keep contrast ratios >= 4.5:1 and validate with a11y tests.",
                    requirement = "UIUX-003"
                )
            )
        }

        return GenericPhaseResult(
            phaseNumber = phaseNumber,
            findings = findings,
            metadata = mapOf("imagesWithoutDesc" to imageNoDesc.size.toString())
        )
    }

    override fun calculateScore(result: PhaseResult): PhaseScore {
        val med = result.findings.count { it.severity == Severity.MEDIUM }
        val low = result.findings.count { it.severity == Severity.LOW }
        val score = (10.0 - med * 2.0 - low * 0.5).coerceIn(1.0, 10.0)
        return PhaseScore(phaseNumber, phaseName, score,
            "UI/UX: $med medium / $low low findings.")
    }
}
