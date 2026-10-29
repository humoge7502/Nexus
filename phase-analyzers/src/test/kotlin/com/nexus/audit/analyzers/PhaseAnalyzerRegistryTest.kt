package com.nexus.audit.analyzers

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class PhaseAnalyzerRegistryTest {

    @Test
    fun `registry exposes all eight phases`() {
        val all = PhaseAnalyzerRegistry.all()
        assertEquals(8, all.size)
        assertEquals((1..8).toSet(), all.keys)
    }

    @Test
    fun `phase numbers and names are consistent`() {
        PhaseAnalyzerRegistry.all().forEach { (number, analyzer) ->
            assertEquals(number, analyzer.phaseNumber)
            assertTrue("Phase $number has a blank name", analyzer.phaseName.isNotBlank())
        }
    }

    @Test
    fun `byPhases filters correctly`() {
        val subset = PhaseAnalyzerRegistry.byPhases(setOf(3, 6))
        assertEquals(setOf(3, 6), subset.keys)
    }
}
