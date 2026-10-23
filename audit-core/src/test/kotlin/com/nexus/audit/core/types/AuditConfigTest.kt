package com.nexus.audit.core.types

import org.junit.Test
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Unit tests for AuditConfig and related types.
 */
class AuditConfigTest {
    
    @Test
    fun `AuditConfig should have correct default values`() {
        val projectRoot = File("/test/project")
        val reportDir = File("/test/reports")
        
        val config = AuditConfig(
            projectRoot = projectRoot,
            reportOutputDir = reportDir
        )
        
        assertEquals(projectRoot, config.projectRoot)
        assertEquals(reportDir, config.reportOutputDir)
        assertEquals((1..8).toSet(), config.enabledPhases)
        assertEquals(setOf(OutputFormat.MARKDOWN), config.outputFormat)
    }
    
    @Test
    fun `ThresholdConfig should have correct default values`() {
        val thresholds = ThresholdConfig()
        
        assertEquals(7, thresholds.minLaunchReadinessScore)
        assertEquals(0, thresholds.maxCriticalIssues)
        assertEquals(5, thresholds.maxHighRiskIssues)
        assertEquals(0.70, thresholds.minTestCoverage, 0.001)
        assertEquals(2000L, thresholds.maxColdStartupMs)
        assertEquals(0, thresholds.maxMemoryLeakCount)
    }
    
    @Test
    fun `OutputFormat should include all required formats`() {
        val formats = OutputFormat.values()
        
        assertTrue(formats.contains(OutputFormat.MARKDOWN))
        assertTrue(formats.contains(OutputFormat.JSON))
        assertTrue(formats.contains(OutputFormat.HTML))
    }
}
