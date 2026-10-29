package com.nexus.audit.core

import com.nexus.audit.core.analyzer.PhaseAnalyzer
import com.nexus.audit.core.types.*
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.junit.Assert.*
import java.io.File

/**
 * Unit tests for AuditOrchestrator.
 * 
 * Tests:
 * - Single phase execution
 * - Multi-phase execution with context sharing
 * - Error handling when phases fail
 * - Deployment recommendation logic
 * 
 * Validates: Requirements 11.1-11.8
 */
class AuditOrchestratorTest {
    
    // Helper function to create a mock phase analyzer
    private fun createMockAnalyzer(
        phaseNum: Int,
        phaseName: String,
        findings: List<Finding> = emptyList(),
        score: Double = 8.0
    ): PhaseAnalyzer {
        return object : PhaseAnalyzer {
            override val phaseNumber = phaseNum
            override val phaseName = phaseName
            
            override suspend fun analyze(context: AuditContext): PhaseResult {
                return GenericPhaseResult(
                    phaseNumber = phaseNum,
                    findings = findings,
                    metadata = mapOf("test" to "data")
                )
            }
            
            override fun calculateScore(result: PhaseResult): PhaseScore {
                return PhaseScore(
                    phaseNumber = phaseNum,
                    phaseName = phaseName,
                    score = score,
                    reasoning = "Test score"
                )
            }
        }
    }
    
    @Test
    fun `runFullAudit should execute all enabled phases`() = runTest {
        // Given: orchestrator with 3 mock phases
        val analyzers = mapOf(
            1 to createMockAnalyzer(1, "Phase 1"),
            2 to createMockAnalyzer(2, "Phase 2"),
            3 to createMockAnalyzer(3, "Phase 3")
        )
        val orchestrator = AuditOrchestrator(analyzers)
        
        val config = AuditConfig(
            projectRoot = File(System.getProperty("java.io.tmpdir")),
            enabledPhases = setOf(1, 2, 3),
            reportOutputDir = File(System.getProperty("java.io.tmpdir"))
        )
        
        // When: running full audit
        val result = orchestrator.runFullAudit(config)
        
        // Then: all phases should be executed
        assertEquals(3, result.phaseResults.size)
        assertTrue(result.phaseResults.containsKey(1))
        assertTrue(result.phaseResults.containsKey(2))
        assertTrue(result.phaseResults.containsKey(3))
        
        // And: scores should be calculated
        assertEquals(3, result.phaseScores.size)
        
        // And: overall scores should be calculated
        assertTrue(result.launchReadinessScore in 1.0..10.0)
        assertTrue(result.startupQualityScore in 1.0..10.0)
    }
    
    @Test
    fun `runPhases should execute only requested phases`() = runTest {
        // Given: orchestrator with 5 phases
        val analyzers = mapOf(
            1 to createMockAnalyzer(1, "Phase 1"),
            2 to createMockAnalyzer(2, "Phase 2"),
            3 to createMockAnalyzer(3, "Phase 3"),
            4 to createMockAnalyzer(4, "Phase 4"),
            5 to createMockAnalyzer(5, "Phase 5")
        )
        val orchestrator = AuditOrchestrator(analyzers)
        
        val config = AuditConfig(
            projectRoot = File(System.getProperty("java.io.tmpdir")),
            reportOutputDir = File(System.getProperty("java.io.tmpdir"))
        )
        
        // When: running only phases 1, 3, 5
        val result = orchestrator.runPhases(listOf(1, 3, 5), config)
        
        // Then: only requested phases should be executed
        assertEquals(3, result.phaseResults.size)
        assertTrue(result.phaseResults.containsKey(1))
        assertFalse(result.phaseResults.containsKey(2))
        assertTrue(result.phaseResults.containsKey(3))
        assertFalse(result.phaseResults.containsKey(4))
        assertTrue(result.phaseResults.containsKey(5))
    }
    
    @Test
    fun `phases should share context through phaseResults`() = runTest {
        // Given: analyzer that checks for previous phase results
        var contextPassed: AuditContext? = null
        
        val phase2Analyzer = object : PhaseAnalyzer {
            override val phaseNumber = 2
            override val phaseName = "Phase 2"
            
            override suspend fun analyze(context: AuditContext): PhaseResult {
                contextPassed = context
                return GenericPhaseResult(
                    phaseNumber = 2,
                    findings = emptyList()
                )
            }
            
            override fun calculateScore(result: PhaseResult): PhaseScore {
                return PhaseScore(2, "Phase 2", 8.0, "Test")
            }
        }
        
        val analyzers = mapOf(
            1 to createMockAnalyzer(1, "Phase 1"),
            2 to phase2Analyzer
        )
        val orchestrator = AuditOrchestrator(analyzers)
        
        val config = AuditConfig(
            projectRoot = File(System.getProperty("java.io.tmpdir")),
            enabledPhases = setOf(1, 2),
            reportOutputDir = File(System.getProperty("java.io.tmpdir"))
        )
        
        // When: running audit
        orchestrator.runFullAudit(config)
        
        // Then: phase 2 should receive phase 1 results in context
        assertNotNull(contextPassed)
        assertTrue(contextPassed!!.phaseResults.containsKey(1))
    }
    
    @Test
    fun `generateDeploymentRecommendation should return NOT_READY when blockers exist`() {
        // Given: orchestrator
        val orchestrator = AuditOrchestrator(emptyMap())
        
        // And: audit result with critical findings (blockers)
        val criticalFinding = Finding(
            severity = Severity.CRITICAL,
            category = "Security",
            title = "Hardcoded API Key",
            description = "API key found in source code",
            recommendation = "Move to secure storage",
            requirement = "6.10"
        )
        
        val result = AuditResult(
            phaseResults = mapOf(
                1 to GenericPhaseResult(1, listOf(criticalFinding))
            ),
            phaseScores = mapOf(
                1 to PhaseScore(1, "Phase 1", 9.0, "Good score")
            ),
            launchReadinessScore = 9.0,
            startupQualityScore = 9.0,
            deploymentDecision = DeploymentDecision(
                recommendation = DeploymentRecommendation.NOT_READY,
                reasoning = "temp",
                topPriorities = emptyList(),
                estimatedEffort = EstimatedEffort(0.0, Complexity.TRIVIAL),
                blockers = emptyList(),
                highRiskSystems = emptyList()
            ),
            executionTime = "PT1S",
            timestamp = "2024-01-01T00:00:00Z"
        )
        
        // When: generating deployment recommendation
        val decision = orchestrator.generateDeploymentRecommendation(result)
        
        // Then: should recommend NOT_READY despite high scores
        assertEquals(DeploymentRecommendation.NOT_READY, decision.recommendation)
        assertTrue(decision.reasoning.contains("blocker"))
        assertEquals(1, decision.blockers.size)
        assertEquals("Hardcoded API Key", decision.blockers[0])
    }
    
    @Test
    fun `generateDeploymentRecommendation should allow READY regardless of low score when no blockers`() {
        // Given: orchestrator
        val orchestrator = AuditOrchestrator(emptyMap())
        
        // And: audit result with NO critical findings but low score
        val lowSeverityFinding = Finding(
            severity = Severity.LOW,
            category = "Code Quality",
            title = "Long method",
            description = "Method exceeds 50 lines",
            recommendation = "Refactor",
            requirement = "3.15"
        )
        
        val result = AuditResult(
            phaseResults = mapOf(
                1 to GenericPhaseResult(1, listOf(lowSeverityFinding))
            ),
            phaseScores = mapOf(
                1 to PhaseScore(1, "Phase 1", 5.0, "Low score")
            ),
            launchReadinessScore = 5.0, // Below threshold of 7
            startupQualityScore = 5.0,
            deploymentDecision = DeploymentDecision(
                recommendation = DeploymentRecommendation.NOT_READY,
                reasoning = "temp",
                topPriorities = emptyList(),
                estimatedEffort = EstimatedEffort(0.0, Complexity.TRIVIAL),
                blockers = emptyList(),
                highRiskSystems = emptyList()
            ),
            executionTime = "PT1S",
            timestamp = "2024-01-01T00:00:00Z"
        )
        
        // When: generating deployment recommendation
        val decision = orchestrator.generateDeploymentRecommendation(result)
        
        // Then: should allow READY because no blockers (score is not absolute blocker)
        assertEquals(DeploymentRecommendation.READY, decision.recommendation)
        assertEquals(0, decision.blockers.size)
        assertEquals(0, decision.highRiskSystems.size)
    }
    
    @Test
    fun `generateDeploymentRecommendation should return READY_WITH_RISKS when high-risk issues exist`() {
        // Given: orchestrator
        val orchestrator = AuditOrchestrator(emptyMap())
        
        // And: audit result with high severity findings but no critical
        val highFinding = Finding(
            severity = Severity.HIGH,
            category = "Performance",
            title = "Memory leak pattern",
            description = "Potential memory leak detected",
            recommendation = "Fix lifecycle handling",
            requirement = "3.7"
        )
        
        val result = AuditResult(
            phaseResults = mapOf(
                1 to GenericPhaseResult(1, listOf(highFinding))
            ),
            phaseScores = mapOf(
                1 to PhaseScore(1, "Phase 1", 8.0, "Good")
            ),
            launchReadinessScore = 8.0,
            startupQualityScore = 8.0,
            deploymentDecision = DeploymentDecision(
                recommendation = DeploymentRecommendation.NOT_READY,
                reasoning = "temp",
                topPriorities = emptyList(),
                estimatedEffort = EstimatedEffort(0.0, Complexity.TRIVIAL),
                blockers = emptyList(),
                highRiskSystems = emptyList()
            ),
            executionTime = "PT1S",
            timestamp = "2024-01-01T00:00:00Z"
        )
        
        // When: generating deployment recommendation
        val decision = orchestrator.generateDeploymentRecommendation(result)
        
        // Then: should recommend READY_WITH_RISKS
        assertEquals(DeploymentRecommendation.READY_WITH_RISKS, decision.recommendation)
        assertEquals(0, decision.blockers.size)
        assertEquals(1, decision.highRiskSystems.size)
    }
    
    @Test
    fun `generateDeploymentRecommendation should return READY when no issues`() {
        // Given: orchestrator
        val orchestrator = AuditOrchestrator(emptyMap())
        
        // And: audit result with no critical or high severity findings
        val result = AuditResult(
            phaseResults = mapOf(
                1 to GenericPhaseResult(1, emptyList())
            ),
            phaseScores = mapOf(
                1 to PhaseScore(1, "Phase 1", 10.0, "Perfect")
            ),
            launchReadinessScore = 10.0,
            startupQualityScore = 10.0,
            deploymentDecision = DeploymentDecision(
                recommendation = DeploymentRecommendation.NOT_READY,
                reasoning = "temp",
                topPriorities = emptyList(),
                estimatedEffort = EstimatedEffort(0.0, Complexity.TRIVIAL),
                blockers = emptyList(),
                highRiskSystems = emptyList()
            ),
            executionTime = "PT1S",
            timestamp = "2024-01-01T00:00:00Z"
        )
        
        // When: generating deployment recommendation
        val decision = orchestrator.generateDeploymentRecommendation(result)
        
        // Then: should recommend READY
        assertEquals(DeploymentRecommendation.READY, decision.recommendation)
        assertEquals(0, decision.blockers.size)
        assertEquals(0, decision.highRiskSystems.size)
    }
    
    @Test
    fun `deployment blockers should have absolute veto power - no override mechanism`() {
        // Given: orchestrator
        val orchestrator = AuditOrchestrator(emptyMap())
        
        // And: audit result with ONE critical blocker but otherwise perfect scores
        val blocker = Finding(
            severity = Severity.CRITICAL,
            category = "Security",
            title = "Missing HTTPS enforcement",
            description = "HTTP traffic allowed",
            recommendation = "Enforce HTTPS",
            requirement = "6.1"
        )
        
        val result = AuditResult(
            phaseResults = mapOf(
                1 to GenericPhaseResult(1, listOf(blocker))
            ),
            phaseScores = mapOf(
                1 to PhaseScore(1, "Phase 1", 10.0, "Perfect except one issue")
            ),
            launchReadinessScore = 10.0, // Perfect score
            startupQualityScore = 10.0,  // Perfect score
            deploymentDecision = DeploymentDecision(
                recommendation = DeploymentRecommendation.NOT_READY,
                reasoning = "temp",
                topPriorities = emptyList(),
                estimatedEffort = EstimatedEffort(0.0, Complexity.TRIVIAL),
                blockers = emptyList(),
                highRiskSystems = emptyList()
            ),
            executionTime = "PT1S",
            timestamp = "2024-01-01T00:00:00Z"
        )
        
        // When: generating deployment recommendation
        val decision = orchestrator.generateDeploymentRecommendation(result)
        
        // Then: MUST be NOT_READY despite perfect scores - blockers have absolute veto
        assertEquals(DeploymentRecommendation.NOT_READY, decision.recommendation)
        assertTrue(decision.reasoning.contains("absolute veto"))
        assertEquals(1, decision.blockers.size)
    }
    
    @Test
    fun `estimatedEffort should be calculated based on finding severity`() {
        // Given: orchestrator
        val orchestrator = AuditOrchestrator(emptyMap())
        
        // And: audit result with multiple severity findings
        val findings = listOf(
            Finding(Severity.CRITICAL, "Cat1", "Issue1", "Desc", null, "Rec", null),
            Finding(Severity.CRITICAL, "Cat2", "Issue2", "Desc", null, "Rec", null),
            Finding(Severity.HIGH, "Cat3", "Issue3", "Desc", null, "Rec", null),
            Finding(Severity.HIGH, "Cat4", "Issue4", "Desc", null, "Rec", null),
            Finding(Severity.MEDIUM, "Cat5", "Issue5", "Desc", null, "Rec", null)
        )
        
        val result = AuditResult(
            phaseResults = mapOf(
                1 to GenericPhaseResult(1, findings)
            ),
            phaseScores = mapOf(
                1 to PhaseScore(1, "Phase 1", 5.0, "Low")
            ),
            launchReadinessScore = 5.0,
            startupQualityScore = 5.0,
            deploymentDecision = DeploymentDecision(
                recommendation = DeploymentRecommendation.NOT_READY,
                reasoning = "temp",
                topPriorities = emptyList(),
                estimatedEffort = EstimatedEffort(0.0, Complexity.TRIVIAL),
                blockers = emptyList(),
                highRiskSystems = emptyList()
            ),
            executionTime = "PT1S",
            timestamp = "2024-01-01T00:00:00Z"
        )
        
        // When: generating deployment recommendation
        val decision = orchestrator.generateDeploymentRecommendation(result)
        
        // Then: effort should be calculated (2 critical * 3 days + 2 high * 2 days + 1 medium * 1 day)
        val expectedDays = (2 * 3.0) + (2 * 2.0) + (1 * 1.0)
        assertEquals(expectedDays, decision.estimatedEffort.developerDays, 0.001)
        
        // And: complexity should reflect critical count
        assertEquals(Complexity.COMPLEX, decision.estimatedEffort.complexity)
    }
    
    @Test
    fun `topPriorities should list top 5 most severe issues`() {
        // Given: orchestrator
        val orchestrator = AuditOrchestrator(emptyMap())
        
        // And: audit result with many findings of different severities
        val findings = listOf(
            Finding(Severity.CRITICAL, "Cat", "Critical1", "Desc", null, "Rec", null),
            Finding(Severity.HIGH, "Cat", "High1", "Desc", null, "Rec", null),
            Finding(Severity.HIGH, "Cat", "High2", "Desc", null, "Rec", null),
            Finding(Severity.MEDIUM, "Cat", "Medium1", "Desc", null, "Rec", null),
            Finding(Severity.MEDIUM, "Cat", "Medium2", "Desc", null, "Rec", null),
            Finding(Severity.LOW, "Cat", "Low1", "Desc", null, "Rec", null),
            Finding(Severity.LOW, "Cat", "Low2", "Desc", null, "Rec", null),
            Finding(Severity.INFO, "Cat", "Info1", "Desc", null, "Rec", null)
        )
        
        val result = AuditResult(
            phaseResults = mapOf(
                1 to GenericPhaseResult(1, findings)
            ),
            phaseScores = mapOf(
                1 to PhaseScore(1, "Phase 1", 6.0, "Medium")
            ),
            launchReadinessScore = 6.0,
            startupQualityScore = 6.0,
            deploymentDecision = DeploymentDecision(
                recommendation = DeploymentRecommendation.NOT_READY,
                reasoning = "temp",
                topPriorities = emptyList(),
                estimatedEffort = EstimatedEffort(0.0, Complexity.TRIVIAL),
                blockers = emptyList(),
                highRiskSystems = emptyList()
            ),
            executionTime = "PT1S",
            timestamp = "2024-01-01T00:00:00Z"
        )
        
        // When: generating deployment recommendation
        val decision = orchestrator.generateDeploymentRecommendation(result)
        
        // Then: should have exactly 5 top priorities
        assertEquals(5, decision.topPriorities.size)
        
        // And: should be sorted by severity (Critical first)
        assertEquals("Critical1", decision.topPriorities[0])
        assertEquals("High1", decision.topPriorities[1])
        assertEquals("High2", decision.topPriorities[2])
    }
}
