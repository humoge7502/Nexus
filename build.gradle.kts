import java.io.File
import java.io.StringReader
import javax.xml.parsers.DocumentBuilderFactory
import org.xml.sax.InputSource
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.hilt) apply false
    alias(libs.plugins.ksp) apply false
    alias(libs.plugins.google.services) apply false
    alias(libs.plugins.firebase.crashlytics) apply false
}

// Convenience alias so the audit is discoverable from the root project:
//   ./gradlew runAudit
tasks.register("runAudit") {
    group = "verification"
    description = "Alias for :audit-cli:runProductionAudit (8-phase production-readiness audit)."
    dependsOn(":audit-cli:runProductionAudit")
}

private data class ModuleCoverage(
    val module: String,
    val covered: Long,
    val missed: Long,
    val floor: Double,
) {
    val coverage: Double get() = if (covered + missed == 0L) 1.0 else covered.toDouble() / (covered + missed)
}

abstract class CoverageGateTask : DefaultTask() {

    @get:InputFile
    abstract val domainReport: RegularFileProperty

    @get:InputFile
    abstract val dataReport: RegularFileProperty

    @get:InputFile
    abstract val networkReport: RegularFileProperty

    @get:OutputFile
    abstract val summaryFile: RegularFileProperty

    @TaskAction
    fun verify() {
        val modules = listOf(
            readCoverage("core:domain", domainReport.get().asFile, floor = 0.40),
            readCoverage("core:data", dataReport.get().asFile, floor = 0.25),
            readCoverage("core:network", networkReport.get().asFile, floor = 0.28),
        )
        val covered = modules.sumOf { it.covered }
        val missed = modules.sumOf { it.missed }
        val aggregate = ModuleCoverage("aggregate", covered, missed, floor = 0.30)

        val summary = buildString {
            appendLine("NEXUS core coverage gate")
            appendLine("module          covered   missed   line%    floor")
            (modules + aggregate).forEach { m ->
                appendLine(
                    "%-15s %7d %8d %6.2f%% %6.1f%%".format(
                        m.module, m.covered, m.missed, m.coverage * 100, m.floor * 100,
                    ),
                )
            }
        }
        summaryFile.get().asFile.apply { parentFile.mkdirs() }.writeText(summary)
        println(summary)

        val failures = (modules + aggregate).filter { it.coverage < it.floor }
        if (failures.isNotEmpty()) {
            throw GradleException(
                "Coverage gate failed: " + failures.joinToString { "${it.module} ${"%.2f".format(it.coverage * 100)}% < ${"%.0f".format(it.floor * 100)}%" } +
                    ". Run ./gradlew coverageGate for the per-module report.",
            )
        }
    }

    private fun readCoverage(module: String, report: File, floor: Double): ModuleCoverage {
        require(report.exists()) { "Missing coverage report for $module at $report" }
        val builder = DocumentBuilderFactory.newInstance().newDocumentBuilder().apply {
            // JaCoCo emits a DOCTYPE pointing at report.dtd; stub it out so report
            // generation never depends on a network/DNS lookup.
            setEntityResolver { _, _ -> InputSource(StringReader("")) }
        }
        val document = report.inputStream().use { builder.parse(it) }
        val lineCounter = with(document.documentElement.childNodes) {
            (0 until length)
                .map { item(it) }
                .filter { it.nodeName == "counter" }
                .map { it.attributes }
                .firstOrNull { it.getNamedItem("type")?.nodeValue == "LINE" }
        } ?: throw GradleException("No LINE counter in $report")
        return ModuleCoverage(
            module = module,
            covered = lineCounter.getNamedItem("covered").nodeValue.toLong(),
            missed = lineCounter.getNamedItem("missed").nodeValue.toLong(),
            floor = floor,
        )
    }
}

tasks.register<CoverageGateTask>("coverageGate") {
    group = "verification"
    description = "Runs core unit-test coverage and enforces per-module + aggregate line-coverage floors."
    dependsOn(
        ":core:domain:createDebugUnitTestCoverageReport",
        ":core:data:createDebugUnitTestCoverageReport",
        ":core:network:createDebugUnitTestCoverageReport",
    )
    domainReport.set(layout.projectDirectory.file("core/domain/build/reports/coverage/test/debug/report.xml"))
    dataReport.set(layout.projectDirectory.file("core/data/build/reports/coverage/test/debug/report.xml"))
    networkReport.set(layout.projectDirectory.file("core/network/build/reports/coverage/test/debug/report.xml"))
    summaryFile.set(layout.buildDirectory.file("reports/coverage-gate/summary.txt"))
}
