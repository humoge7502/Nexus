package com.nexus.audit.analyzers

import com.nexus.audit.core.analyzer.PhaseAnalyzer
import com.nexus.audit.core.types.*
import com.nexus.audit.rules.SecurityRules
import java.io.File

/**
 * Phase 6 — Security.
 *
 * Detects hardcoded secrets, TLS verification bypass, plaintext HTTP endpoints,
 * risky third-party hosts, and backup misconfiguration.
 */
class SecurityAnalyzer : PhaseAnalyzer {
    override val phaseNumber = 6
    override val phaseName = "Security"

    private val secretPattern = Regex(
        """(api[_-]?key|secret|token|password|passwd|client[_-]?secret|access[_-]?key)\s*[:=]\s*["']([A-Za-z0-9_\-]{16,})["']""",
        RegexOption.IGNORE_CASE
    )
    private val sslBypass = Regex(
        """setHostnameVerifier\s*\(|ALLOW_ALL_HOSTNAME_VERIFIER|checkClientTrusted\s*\(.*\)\s*\{\s*\}|TrustManager\s*\{[\s\S]*?void\s+check""",
        RegexOption.IGNORE_CASE
    )
    private val httpEndpoint = Regex("""https?://[a-zA-Z0-9.\-]+\.[a-z]{2,}""")

    override suspend fun analyze(context: AuditContext): PhaseResult {
        val files = (ScanUtil.productionSources(context.sourceFiles) +
            context.buildFiles.filter { !it.absolutePath.contains("/build/") })
            .distinctBy { it.absolutePath }
        val findings = mutableListOf<Finding>()

        // Hardcoded secrets
        val secretHits = ScanUtil.grep(files, secretPattern)
        secretHits.distinctBy { it.file.absolutePath to it.line }.take(10).forEach { hit ->
            findings.add(
                ScanUtil.finding(
                    severity = Severity.CRITICAL,
                    category = "Security",
                    title = SecurityRules.HARDCODED_SECRET.title,
                    description = SecurityRules.HARDCODED_SECRET.description +
                        " Snippet: ${hit.text.take(80)}",
                    recommendation = SecurityRules.HARDCODED_SECRET.remediation +
                        " Rule ${SecurityRules.HARDCODED_SECRET.id}.",
                    hit = hit,
                    requirement = SecurityRules.HARDCODED_SECRET.id
                )
            )
        }

        // TLS bypass
        val sslHits = ScanUtil.grep(files, sslBypass)
        sslHits.forEach { hit ->
            findings.add(
                ScanUtil.finding(
                    severity = Severity.CRITICAL,
                    category = "Security",
                    title = SecurityRules.SSL_BYPASS.title,
                    description = SecurityRules.SSL_BYPASS.description,
                    recommendation = SecurityRules.SSL_BYPASS.remediation +
                        " Rule ${SecurityRules.SSL_BYPASS.id}.",
                    hit = hit,
                    requirement = SecurityRules.SSL_BYPASS.id
                )
            )
        }

        // Plaintext HTTP endpoints
        val httpHits = ScanUtil.grep(files, httpEndpoint)
            .filter { it.text.contains("http://") }
        httpHits.take(10).forEach { hit ->
            findings.add(
                ScanUtil.finding(
                    severity = Severity.HIGH,
                    category = "Security",
                    title = SecurityRules.INSECURE_ENDPOINT.title,
                    description = SecurityRules.INSECURE_ENDPOINT.description + " Found: ${hit.text.take(80)}",
                    recommendation = SecurityRules.INSECURE_ENDPOINT.remediation +
                        " Rule ${SecurityRules.INSECURE_ENDPOINT.id}.",
                    hit = hit,
                    requirement = SecurityRules.INSECURE_ENDPOINT.id
                )
            )
        }

        // Unreliable third-party host
        val risky = ScanUtil.grep(files, Regex("""onrender\.com|herokuapp\.com|ngrok|pythonanywhere"""))
        risky.forEach { hit ->
            findings.add(
                ScanUtil.finding(
                    severity = Severity.HIGH,
                    category = "Security",
                    title = SecurityRules.UNRELIABLE_THIRD_PARTY.title,
                    description = SecurityRules.UNRELIABLE_THIRD_PARTY.description,
                    recommendation = SecurityRules.UNRELIABLE_THIRD_PARTY.remediation +
                        " Rule ${SecurityRules.UNRELIABLE_THIRD_PARTY.id}.",
                    hit = hit,
                    requirement = SecurityRules.UNRELIABLE_THIRD_PARTY.id
                )
            )
        }

        // Manifest backup flag
        val manifest = File(context.projectRoot, "app/src/main/AndroidManifest.xml")
        if (manifest.exists()) {
            val m = ScanUtil.readQuietly(manifest)
            if (m.contains("android:allowBackup=\"true\"") && !m.contains("android:fullBackupContent") &&
                !m.contains("android:dataExtractionRules")) {
                findings.add(
                    ScanUtil.finding(
                        severity = Severity.MEDIUM,
                        category = "Security",
                        title = SecurityRules.ALLOW_BACKUP.title,
                        description = SecurityRules.ALLOW_BACKUP.description,
                        recommendation = SecurityRules.ALLOW_BACKUP.remediation +
                            " Rule ${SecurityRules.ALLOW_BACKUP.id}.",
                        requirement = SecurityRules.ALLOW_BACKUP.id
                    )
                )
            }
        }

        if (findings.isEmpty()) {
            findings.add(
                ScanUtil.finding(
                    severity = Severity.INFO,
                    category = "Security",
                    title = "No critical security issues detected",
                    description = "Static scan found no hardcoded secrets, TLS bypass, or plaintext HTTP endpoints.",
                    recommendation = "Add gitleaks to CI and a release-build secret-assertion test to keep this green.",
                    requirement = SecurityRules.KEY_RELEASE_SAFE.id
                )
            )
        }

        return GenericPhaseResult(
            phaseNumber = phaseNumber,
            findings = findings,
            metadata = mapOf(
                "secrets" to secretHits.size.toString(),
                "httpEndpoints" to httpHits.size.toString()
            )
        )
    }

    override fun calculateScore(result: PhaseResult): PhaseScore {
        val crit = result.findings.count { it.severity == Severity.CRITICAL }
        val high = result.findings.count { it.severity == Severity.HIGH }
        val med = result.findings.count { it.severity == Severity.MEDIUM }
        val score = (10.0 - crit * 4.0 - high * 2.0 - med * 1.0).coerceIn(1.0, 10.0)
        return PhaseScore(phaseNumber, phaseName, score,
            "Security: $crit critical / $high high / $med medium findings.")
    }
}
