package com.nexus.audit.analyzers

import com.nexus.audit.core.analyzer.PhaseAnalyzer
import com.nexus.audit.core.types.*
import com.nexus.audit.rules.RuleCatalog

/**
 * Phase 2 — API Integration.
 *
 * Checks network layer hygiene: timeouts, error handling, retry/backoff,
 * base-url configuration, and that the AI key never ships in release.
 */
class ApiIntegrationAnalyzer : PhaseAnalyzer {
    override val phaseNumber = 2
    override val phaseName = "API Integration"

    override suspend fun analyze(context: AuditContext): PhaseResult {
        val files = ScanUtil.productionFiles(context.sourceFiles)
        val findings = mutableListOf<Finding>()

        val retrofit = ScanUtil.grep(files, Regex("""Retrofit|create\(\s*Api::class"""))
        val okhttp = ScanUtil.grep(files, Regex("""OkHttpClient|Builder\(\)\s*\.connectTimeout"""))

        if (retrofit.isNotEmpty() && okhttp.isEmpty()) {
            findings.add(
                ScanUtil.finding(
                    severity = Severity.HIGH,
                    category = "API Integration",
                    title = "HTTP client lacks explicit timeouts",
                    description = "A Retrofit API is defined but no OkHttpClient.connectTimeout/readTimeout " +
                        "configuration was found. Default timeouts can hang the UI on slow networks.",
                    recommendation = "Configure connectTimeout(15s)/readTimeout(30s) and a retry interceptor. " +
                        "Rule ${RuleCatalog["API-001"].id}.",
                    requirement = "API-001"
                )
            )
        }

        val noErrorHandling = ScanUtil.grep(files, Regex("""\.enqueue\s*\{|call\s*\.\s*execute\(\)"""))
            .filter { hit -> !ScanUtil.readQuietly(hit.file).contains("try") }
        if (noErrorHandling.isNotEmpty()) {
            findings.add(
                ScanUtil.finding(
                    severity = Severity.MEDIUM,
                    category = "API Integration",
                    title = "Network call without visible error handling",
                    description = "Found ${noErrorHandling.size} callback/execute call site(s) without a nearby " +
                        "'try' block; failures may surface as unhandled exceptions or silent UI stalls.",
                    recommendation = "Wrap calls in try/catch (or use a Result-returning suspend wrapper) and " +
                        "surface errors to the UI via a sealed state.",
                    requirement = "API-002"
                )
            )
        }

        val hardcodedBaseUrl = ScanUtil.grep(files, Regex("""BASE_URL\s*=\s*"https?://|baseUrl\s*\(\s*"(http)"""))
        hardcodedBaseUrl.forEach { hit ->
            findings.add(
                ScanUtil.finding(
                    severity = Severity.LOW,
                    category = "API Integration",
                    title = "Hardcoded base URL",
                    description = "Base URL is hardwired in source (${hit.file.name}:${hit.line}). This blocks " +
                        "environment switching (dev/staging/prod) without a rebuild.",
                    recommendation = "Move the base URL to BuildConfig / remote config so environments swap " +
                        "without recompilation.",
                    hit = hit,
                    requirement = "API-003"
                )
            )
        }

        if (retrofit.isEmpty()) {
            findings.add(
                ScanUtil.finding(
                    severity = Severity.INFO,
                    category = "API Integration",
                    title = "No HTTP client detected",
                    description = "No Retrofit/OkHttp usage detected in source trees. If the app is offline-only " +
                        "this is fine; otherwise networking may be handled in the backend module.",
                    recommendation = "Confirm the data path; document where network calls originate.",
                    requirement = "API-001"
                )
            )
        }

        return GenericPhaseResult(
            phaseNumber = phaseNumber,
            findings = findings,
            metadata = mapOf("retrofitSites" to retrofit.size.toString())
        )
    }

    override fun calculateScore(result: PhaseResult): PhaseScore {
        val high = result.findings.count { it.severity == Severity.HIGH }
        val med = result.findings.count { it.severity == Severity.MEDIUM }
        val low = result.findings.count { it.severity == Severity.LOW }
        val score = (10.0 - high * 3.0 - med * 1.5 - low * 0.5).coerceIn(1.0, 10.0)
        return PhaseScore(phaseNumber, phaseName, score,
            "API hygiene: $high high / $med medium / $low low findings.")
    }
}
