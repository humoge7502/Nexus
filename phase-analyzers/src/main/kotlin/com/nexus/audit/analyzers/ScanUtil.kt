package com.nexus.audit.analyzers

import com.nexus.audit.core.types.CodeLocation
import com.nexus.audit.core.types.Finding
import com.nexus.audit.core.types.Severity
import java.io.File

/**
 * Shared static-analysis helpers used by every phase analyzer.
 * Keeps the per-phase code small and the detection logic uniform.
 */
internal object ScanUtil {
    private val EXCLUDE_DIRS = setOf("build", ".gradle", ".idea", ".git", ".kotlin", "node_modules")

    // The audit tool's own sources embed the regexes/rules it looks for, so
    // scanning them would always self-match (e.g. the TLS-bypass pattern).
    private val EXCLUDE_MODULES = setOf(
        "audit-core", "audit-rules", "audit-reporting", "audit-scanner", "audit-cli", "phase-analyzers"
    )

    /** Drop build/generated/IDE noise plus the audit tool's own modules from a file list. */
    fun productionFiles(all: List<File>): List<File> = all.filter { f ->
        EXCLUDE_DIRS.none { seg -> f.absolutePath.contains("/$seg/") || f.absolutePath.contains("\\$seg\\") } &&
            EXCLUDE_MODULES.none { seg -> f.absolutePath.contains("/$seg/") || f.absolutePath.contains("\\$seg\\") }
    }

    /** Drop test source sets (unit + instrumented) from a production file list. */
    fun productionSources(all: List<File>): List<File> = productionFiles(all).filterNot { isTestFile(it) }

    fun readQuietly(file: File): String = try { file.readText() } catch (_: Exception) { "" }

    data class Hit(val file: File, val line: Int, val text: String)

    /** Returns every line (with number) matching [regex] across [files]. */
    fun grep(files: List<File>, regex: Regex, ignoreCase: Boolean = true): List<Hit> {
        val opts = if (ignoreCase) setOf(RegexOption.IGNORE_CASE) else emptySet()
        val re = if (opts.isEmpty()) regex else Regex(regex.pattern, regex.options + opts)
        val hits = mutableListOf<Hit>()
        for (file in files) {
            val text = readQuietly(file)
            var suppressNext = false
            text.lineSequence().forEachIndexed { idx, line ->
                // `// audit-allow: RULE-ID — reason` suppresses this line, or the
                // next line when the marker sits on its own line above the code.
                if (line.contains("audit-allow:")) {
                    if (!re.containsMatchIn(line)) suppressNext = true
                    return@forEachIndexed
                }
                if (suppressNext) {
                    suppressNext = false
                    return@forEachIndexed
                }
                if (re.containsMatchIn(line)) hits.add(Hit(file, idx + 1, line.trim().take(240)))
            }
        }
        return hits
    }

    fun lineCount(file: File): Int {
        val t = readQuietly(file)
        return if (t.isEmpty()) 0 else t.count { it == '\n' } + 1
    }

    fun finding(
        severity: Severity,
        category: String,
        title: String,
        description: String,
        recommendation: String,
        hit: Hit? = null,
        requirement: String? = null
    ): Finding = Finding(
        severity = severity,
        category = category,
        title = title,
        description = description,
        location = hit?.let { CodeLocation(it.file, it.line, snippet = it.text) },
        recommendation = recommendation,
        requirement = requirement
    )

    /** True when [path] is inside a test source set. */
    fun isTestFile(file: File): Boolean {
        val p = file.absolutePath
        return p.contains("/test/") || p.contains("/androidTest/") ||
            p.contains("\\test\\") || p.contains("\\androidTest\\") ||
            file.name.endsWith("Test.kt") || file.name.endsWith("Tests.kt")
    }

    /** Path of [file] relative to [root], falling back to the absolute path. */
    fun relativePath(file: File, root: File): String = try {
        file.relativeTo(root).path
    } catch (_: Exception) {
        file.absolutePath
    }
}
