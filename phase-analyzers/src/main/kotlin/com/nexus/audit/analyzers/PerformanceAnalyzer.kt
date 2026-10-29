package com.nexus.audit.analyzers

import com.nexus.audit.core.analyzer.PhaseAnalyzer
import com.nexus.audit.core.types.*

/**
 * Phase 5 — Performance.
 *
 * Flags main-thread blocking, missing caching/pagination, and unbounded
 * list rendering that can cause jank.
 */
class PerformanceAnalyzer : PhaseAnalyzer {
    override val phaseNumber = 5
    override val phaseName = "Performance"

    override suspend fun analyze(context: AuditContext): PhaseResult {
        val files = ScanUtil.productionSources(context.sourceFiles)
        val findings = mutableListOf<Finding>()

        // synchronized / blocking IO on main
        val sync = ScanUtil.grep(files, Regex("""synchronized\s*\(|Thread\.sleep\s*\("""))
        sync.forEach { hit ->
            findings.add(
                ScanUtil.finding(
                    severity = Severity.HIGH,
                    category = "Performance",
                    title = "Blocking primitive on UI path",
                    description = "synchronized/Thread.sleep can block the UI thread and cause frame drops. " +
                        "Found in ${hit.file.name}:${hit.line}.",
                    recommendation = "Move work to a background dispatcher (Dispatchers.IO/Default) and use " +
                        "coroutine mutex instead of synchronized.",
                    hit = hit,
                    requirement = "PERF-001"
                )
            )
        }

        // LazyColumn without item keys (can cause recomposition waste)
        val lazy = ScanUtil.grep(files, Regex("""LazyColumn\s*\{|LazyRow\s*\{|LazyVerticalGrid\s*\{"""))
        val noKey = lazy.filter { hit ->
            val tail = ScanUtil.readQuietly(hit.file).lineSequence().drop(hit.line).take(20)
            tail.none { it.contains("items(") && it.contains("key") } &&
                tail.none { it.contains("itemsIndexed") }
        }
        if (noKey.isNotEmpty()) {
            findings.add(
                ScanUtil.finding(
                    severity = Severity.MEDIUM,
                    category = "Performance",
                    title = "Lazy list without stable item keys",
                    description = "Found ${noKey.size} lazy list(s) that may render without stable keys, risking " +
                        "unnecessary recomposition on data changes.",
                    recommendation = "Use items(items, key = { it.id }) { ... } so Compose can diff efficiently.",
                    hit = noKey.first(),
                    requirement = "PERF-002"
                )
            )
        }

        // No image caching library reference
        val hasCoil = ScanUtil.grep(files, Regex("""AsyncImage|rememberAsyncImagePainter|Coil|Glide|Picasso"""))
        if (hasCoil.isEmpty()) {
            findings.add(
                ScanUtil.finding(
                    severity = Severity.LOW,
                    category = "Performance",
                    title = "No image-loading/caching library detected",
                    description = "No Coil/Glide/Picasso usage found. Ad-hoc image loading often misses caching, " +
                        "memory trimming, and placeholder handling.",
                    recommendation = "Adopt Coil (Compose-native) for cached, lifecycle-aware image loading.",
                    requirement = "PERF-003"
                )
            )
        }

        return GenericPhaseResult(
            phaseNumber = phaseNumber,
            findings = findings,
            metadata = mapOf("lazyLists" to lazy.size.toString())
        )
    }

    override fun calculateScore(result: PhaseResult): PhaseScore {
        val high = result.findings.count { it.severity == Severity.HIGH }
        val med = result.findings.count { it.severity == Severity.MEDIUM }
        val low = result.findings.count { it.severity == Severity.LOW }
        val score = (10.0 - high * 3.0 - med * 1.5 - low * 0.5).coerceIn(1.0, 10.0)
        return PhaseScore(phaseNumber, phaseName, score,
            "Performance: $high high / $med medium / $low low findings.")
    }
}
