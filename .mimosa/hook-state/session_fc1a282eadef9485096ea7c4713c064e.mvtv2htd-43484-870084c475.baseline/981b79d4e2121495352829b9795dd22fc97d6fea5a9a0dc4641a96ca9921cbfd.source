package com.nexus.audit.analyzers

import com.nexus.audit.core.types.AuditContext
import com.nexus.audit.core.types.DependencyGraph
import com.nexus.audit.core.types.ModuleGraph
import com.nexus.audit.core.types.Severity
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import java.nio.file.Files

class SecurityAnalyzerTest {

    private fun contextFor(root: File, files: List<File>) = AuditContext(
        projectRoot = root,
        sourceFiles = files,
        buildFiles = emptyList(),
        moduleStructure = ModuleGraph(emptyList(), emptyMap()),
        dependencies = DependencyGraph(emptyList(), emptyMap())
    )

    @Test
    fun `detects hardcoded secret and plaintext endpoint`() = runBlocking {
        val root = Files.createTempDirectory("nexus-sec").toFile()
        try {
            val kt = File(root, "Leaky.kt")
            kt.writeText(
                """
                object Leaky {
                    val apiKey = "sk-abcdefghijklmnopqrstuvwxyz123456"
                    val endpoint = "http://insecure.example.com/api"
                }
                """.trimIndent()
            )

            val result = SecurityAnalyzer().analyze(contextFor(root, listOf(kt)))
            val severities = result.findings.map { it.severity }

            assertTrue("expected a CRITICAL secret finding", severities.contains(Severity.CRITICAL))
            assertTrue("expected a HIGH plaintext endpoint finding", severities.contains(Severity.HIGH))
        } finally {
            root.deleteRecursively()
        }
    }

    @Test
    fun `flags allowBackup without extraction rules`() = runBlocking {
        val root = Files.createTempDirectory("nexus-backup").toFile()
        try {
            val manifest = File(root, "app/src/main/AndroidManifest.xml")
            manifest.parentFile.mkdirs()
            manifest.writeText("""<manifest><application android:allowBackup="true"/></manifest>""")

            val result = SecurityAnalyzer().analyze(contextFor(root, emptyList()))
            val backup = result.findings.filter { it.severity == Severity.MEDIUM }

            assertTrue("expected an allowBackup finding", backup.isNotEmpty())
        } finally {
            root.deleteRecursively()
        }
    }

    @Test
    fun `audit-allow suppresses a finding`() = runBlocking {
        val root = Files.createTempDirectory("nexus-allow").toFile()
        try {
            val kt = File(root, "Suppressed.kt")
            kt.writeText(
                """
                object Suppressed {
                    // audit-allow: SEC-003 — internal loopback used only in tests
                    val endpoint = "http://insecure.example.com/api"
                }
                """.trimIndent()
            )

            val result = SecurityAnalyzer().analyze(contextFor(root, listOf(kt)))
            val httpFindings = result.findings.filter {
                it.title.contains("Plaintext HTTP", ignoreCase = true)
            }
            assertTrue("suppressed finding should not be reported", httpFindings.isEmpty())
        } finally {
            root.deleteRecursively()
        }
    }
}
