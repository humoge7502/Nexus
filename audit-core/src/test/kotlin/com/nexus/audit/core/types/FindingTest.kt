package com.nexus.audit.core.types

import org.junit.Test
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Unit tests for Finding and related types.
 */
class FindingTest {
    
    @Test
    fun `Finding should be created with all fields`() {
        val file = File("/test/File.kt")
        val location = CodeLocation(
            file = file,
            line = 42,
            column = 10,
            snippet = "val x = 1"
        )
        
        val finding = Finding(
            severity = Severity.CRITICAL,
            category = "Architecture",
            title = "Memory Leak Detected",
            description = "Potential memory leak in ViewModel",
            location = location,
            recommendation = "Fix the leak",
            requirement = "3.7"
        )
        
        assertEquals(Severity.CRITICAL, finding.severity)
        assertEquals("Architecture", finding.category)
        assertEquals("Memory Leak Detected", finding.title)
        assertEquals(location, finding.location)
        assertEquals("3.7", finding.requirement)
    }
    
    @Test
    fun `Finding can be created without location`() {
        val finding = Finding(
            severity = Severity.INFO,
            category = "General",
            title = "Info message",
            description = "Just information",
            recommendation = "No action needed"
        )
        
        assertNull(finding.location)
        assertNull(finding.requirement)
    }
    
    @Test
    fun `Severity should have correct priority levels`() {
        val severities = Severity.values()
        
        assertEquals(5, severities.size)
        assertEquals(Severity.CRITICAL, severities[0])
        assertEquals(Severity.HIGH, severities[1])
        assertEquals(Severity.MEDIUM, severities[2])
        assertEquals(Severity.LOW, severities[3])
        assertEquals(Severity.INFO, severities[4])
    }
}
