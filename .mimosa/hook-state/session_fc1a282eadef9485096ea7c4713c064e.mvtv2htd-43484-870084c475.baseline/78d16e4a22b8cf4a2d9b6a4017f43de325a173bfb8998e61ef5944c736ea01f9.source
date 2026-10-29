package com.nexus.audit.analyzers

import com.nexus.audit.core.analyzer.PhaseAnalyzer
import com.nexus.audit.core.types.*
import java.io.File

/**
 * Phase 7 — Deployment Readiness.
 *
 * Verifies CI, code signing, versioning, and release hardening (R8/minify).
 */
class DeploymentAnalyzer : PhaseAnalyzer {
    override val phaseNumber = 7
    override val phaseName = "Deployment Readiness"

    override suspend fun analyze(context: AuditContext): PhaseResult {
        val root = context.projectRoot
        val findings = mutableListOf<Finding>()

        // CI presence
        val ciDir = File(root, ".github/workflows")
        val ciFiles = if (ciDir.exists()) ciDir.listFiles()?.filter { it.extension == "yml" || it.extension == "yaml" } ?: emptyList() else emptyList()
        if (ciFiles.isEmpty()) {
            findings.add(
                ScanUtil.finding(
                    severity = Severity.HIGH,
                    category = "Deployment",
                    title = "No CI pipeline configured",
                    description = "No GitHub Actions workflows found. Builds/releases are manual and error-prone.",
                    recommendation = "Add a CI workflow running assembleRelease, unit tests, and the audit task.",
                    requirement = "DEPLOY-001"
                )
            )
        } else {
            findings.add(
                ScanUtil.finding(
                    severity = Severity.INFO,
                    category = "Deployment",
                    title = "CI pipeline present",
                    description = "Found ${ciFiles.size} workflow file(s): ${ciFiles.joinToString { it.name }}.",
                    recommendation = "Ensure the pipeline runs the audit + detekt + dependency check on every PR.",
                    requirement = "DEPLOY-001"
                )
            )
        }

        // App signing
        val appBuild = File(root, "app/build.gradle.kts")
        if (appBuild.exists()) {
            val t = ScanUtil.readQuietly(appBuild)
            if (!t.contains("signingConfigs") && !t.contains("getSigningConfig")) {
                findings.add(
                    ScanUtil.finding(
                        severity = Severity.MEDIUM,
                        category = "Deployment",
                        title = "No explicit release signing config",
                        description = "app/build.gradle.kts has no signingConfigs block. Release builds will fail " +
                            "or rely on implicit/default signing.",
                        recommendation = "Add a signingConfigs block wired to environment-provided keystore secrets.",
                        requirement = "DEPLOY-002"
                    )
                )
            }
            val minifyOn = Regex("""(isMinifyEnabled|minifyEnabled)\s*=?\s*true""").containsMatchIn(t)
            if (!minifyOn) {
                findings.add(
                    ScanUtil.finding(
                        severity = Severity.MEDIUM,
                        category = "Deployment",
                        title = "R8/minify not enabled for release",
                        description = "No minifyEnabled true in release build type. App ships unshrunk, larger, and " +
                            "easier to reverse-engineer.",
                        recommendation = "Enable minifyEnabled + shrinkResources and verify with R8 mapping upload.",
                        requirement = "DEPLOY-003"
                    )
                )
            }
            if (!t.contains("versionCode") && !t.contains("versionName")) {
                findings.add(
                    ScanUtil.finding(
                        severity = Severity.LOW,
                        category = "Deployment",
                        title = "No versionCode/versionName declared",
                        description = "App version metadata not found in app/build.gradle.kts.",
                        recommendation = "Declare versionCode/versionName (ideally from CI) for store releases.",
                        requirement = "DEPLOY-004"
                    )
                )
            }
        } else {
            findings.add(
                ScanUtil.finding(
                    severity = Severity.MEDIUM,
                    category = "Deployment",
                    title = "app module build file missing",
                    description = "Expected app/build.gradle.kts not found.",
                    recommendation = "Ensure the app module declares its build configuration.",
                    requirement = "DEPLOY-002"
                )
            )
        }

        return GenericPhaseResult(
            phaseNumber = phaseNumber,
            findings = findings,
            metadata = mapOf("ciFiles" to ciFiles.size.toString())
        )
    }

    override fun calculateScore(result: PhaseResult): PhaseScore {
        val high = result.findings.count { it.severity == Severity.HIGH }
        val med = result.findings.count { it.severity == Severity.MEDIUM }
        val low = result.findings.count { it.severity == Severity.LOW }
        val score = (10.0 - high * 3.0 - med * 1.5 - low * 0.5).coerceIn(1.0, 10.0)
        return PhaseScore(phaseNumber, phaseName, score,
            "Deployment: $high high / $med medium / $low low findings.")
    }
}
