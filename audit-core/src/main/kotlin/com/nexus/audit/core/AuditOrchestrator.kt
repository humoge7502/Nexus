package com.nexus.audit.core

import com.nexus.audit.core.analyzer.PhaseAnalyzer
import com.nexus.audit.core.types.*
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import java.io.File
import java.time.Instant
import kotlin.time.Duration
import kotlin.time.measureTime

/**
 * Main orchestrator that coordinates execution of all 8 audit phases.
 * 
 * This is the core orchestration engine that:
 * - Manages phase execution order and dependencies
 * - Builds shared context (AuditContext) for phase analyzers
 * - Aggregates results across all phases
 * - Calculates launch readiness and startup quality scores
 * - Generates deployment recommendations based on critical rules
 * 
 * CRITICAL RULES:
 * 1. ANY deployment blocker forces "Not Ready" with no exceptions
 * 2. "Ready" recommendations ARE allowed regardless of Launch_Readiness_Score
 * 3. Deployment blockers have absolute veto power
 * 
 * Validates: Requirements 11.1, 11.2, 11.3, 11.4, 11.8
 */
class AuditOrchestrator(
    private val phaseAnalyzers: Map<Int, PhaseAnalyzer>
) {
    
    /**
     * Executes all audit phases and generates comprehensive report.
     * 
     * @param config Configuration specifying which phases to run and thresholds
     * @return Complete audit result with findings from all phases
     */
    suspend fun runFullAudit(config: AuditConfig): AuditResult {
        val executionTime = measureTime {
            return runAuditInternal(config)
        }
        
        // Adjust timing in the result
        val result = runAuditInternal(config)
        return result.copy(executionTime = executionTime.toIsoString())
    }
    
    /**
     * Executes specific audit phases.
     * 
     * @param phases List of phases to execute (1-8)
     * @param config Configuration for the audit
     * @return Audit result with findings from requested phases
     */
    suspend fun runPhases(phases: List<Int>, config: AuditConfig): AuditResult {
        val adjustedConfig = config.copy(enabledPhases = phases.toSet())
        return runFullAudit(adjustedConfig)
    }
    
    /**
     * Generates deployment recommendation based on audit results.
     * 
     * CRITICAL RULES:
     * 1. If ANY Deployment_Blocker exists, MUST return "Not Ready" with no exceptions
     * 2. 'Ready' recommendations ARE allowed regardless of Launch_Readiness_Score value
     * 3. Launch_Readiness_Score below threshold is a factor but not an absolute blocker
     * 4. Deployment blockers have absolute veto power over deployment decisions
     * 
     * @param result Complete audit result
     * @return Deployment decision with recommendation and reasoning
     */
    fun generateDeploymentRecommendation(result: AuditResult): DeploymentDecision {
        // Extract all findings across all phases
        val allFindings = result.phaseResults.values.flatMap { it.findings }
        
        // Identify deployment blockers (CRITICAL severity findings)
        val blockers = allFindings
            .filter { it.severity == Severity.CRITICAL }
            .map { it.title }
        
        // Identify high-risk systems (HIGH severity findings)
        val highRiskSystems = allFindings
            .filter { it.severity == Severity.HIGH }
            .map { it.title }
        
        // Calculate top priorities (top 5 most critical issues)
        val topPriorities = allFindings
            .sortedByDescending { 
                when (it.severity) {
                    Severity.CRITICAL -> 5
                    Severity.HIGH -> 4
                    Severity.MEDIUM -> 3
                    Severity.LOW -> 2
                    Severity.INFO -> 1
                }
            }
            .take(5)
            .map { it.title }
        
        // Estimate effort to fix critical and high severity issues
        val criticalCount = allFindings.count { it.severity == Severity.CRITICAL }
        val highCount = allFindings.count { it.severity == Severity.HIGH }
        val mediumCount = allFindings.count { it.severity == Severity.MEDIUM }
        
        val estimatedDays = (criticalCount * 3.0) + (highCount * 2.0) + (mediumCount * 1.0)
        val complexity = when {
            criticalCount > 5 -> Complexity.VERY_COMPLEX
            criticalCount > 2 -> Complexity.COMPLEX
            highCount > 10 -> Complexity.COMPLEX
            highCount > 5 -> Complexity.MODERATE
            mediumCount > 10 -> Complexity.MODERATE
            else -> Complexity.SIMPLE
        }
        
        val estimatedEffort = EstimatedEffort(
            developerDays = estimatedDays,
            complexity = complexity
        )
        
        // CRITICAL RULE: ANY deployment blocker forces "Not Ready" with no exceptions
        if (blockers.isNotEmpty()) {
            return DeploymentDecision(
                recommendation = DeploymentRecommendation.NOT_READY,
                reasoning = "Deployment blockers detected: ${blockers.size} critical issue(s) must be " +
                        "resolved before production deployment. These issues have absolute veto power " +
                        "over deployment decisions regardless of overall scores.",
                topPriorities = topPriorities,
                estimatedEffort = estimatedEffort,
                blockers = blockers,
                highRiskSystems = highRiskSystems
            )
        }
        
        // No blockers exist - now consider high-risk systems and scores
        // Note: Low scores are a factor but NOT an absolute blocker when no critical issues exist
        
        if (highRiskSystems.isNotEmpty()) {
            return DeploymentDecision(
                recommendation = DeploymentRecommendation.READY_WITH_RISKS,
                reasoning = "No deployment blockers detected, but ${highRiskSystems.size} high-risk " +
                        "system(s) identified. Launch readiness score: ${result.launchReadinessScore}/10. " +
                        "Deployment is possible but requires careful monitoring and risk mitigation plans.",
                topPriorities = topPriorities,
                estimatedEffort = estimatedEffort,
                blockers = blockers,
                highRiskSystems = highRiskSystems
            )
        }
        
        // No blockers and no high-risk systems - recommend "Ready"
        // This is allowed regardless of Launch_Readiness_Score value
        return DeploymentDecision(
            recommendation = DeploymentRecommendation.READY,
            reasoning = "No deployment blockers or high-risk systems detected. " +
                    "Launch readiness score: ${result.launchReadinessScore}/10. " +
                    "Startup quality score: ${result.startupQualityScore}/10. " +
                    "Application is ready for production deployment.",
            topPriorities = topPriorities.ifEmpty { listOf("Continue monitoring production metrics") },
            estimatedEffort = estimatedEffort,
            blockers = blockers,
            highRiskSystems = highRiskSystems
        )
    }
    
    /**
     * Internal method that performs the actual audit execution.
     */
    private suspend fun runAuditInternal(config: AuditConfig): AuditResult = coroutineScope {
        // Build shared context
        val context = buildAuditContext(config)
        
        // Resolve phase execution order (respects dependencies)
        val executionOrder = resolvePhaseExecutionOrder(config.enabledPhases)
        
        // Execute phases in order, accumulating results in context
        val phaseResults = mutableMapOf<Int, PhaseResult>()
        val phaseScores = mutableMapOf<Int, PhaseScore>()
        
        for (phaseNumber in executionOrder) {
            val analyzer = phaseAnalyzers[phaseNumber]
                ?: throw IllegalStateException("No analyzer found for phase $phaseNumber")
            
            // Execute phase analysis
            val result = analyzer.analyze(context)
            phaseResults[phaseNumber] = result
            
            // Calculate score for this phase
            val score = analyzer.calculateScore(result)
            phaseScores[phaseNumber] = score
            
            // Add result to context for subsequent phases
            context.phaseResults[phaseNumber] = result
        }
        
        // Calculate overall scores
        val launchReadinessScore = calculateLaunchReadinessScore(phaseScores, config.thresholds)
        val startupQualityScore = calculateStartupQualityScore(phaseScores)
        
        // Create preliminary result for deployment decision
        val preliminaryResult = AuditResult(
            phaseResults = phaseResults,
            phaseScores = phaseScores,
            launchReadinessScore = launchReadinessScore,
            startupQualityScore = startupQualityScore,
            deploymentDecision = DeploymentDecision(
                recommendation = DeploymentRecommendation.NOT_READY,
                reasoning = "Preliminary",
                topPriorities = emptyList(),
                estimatedEffort = EstimatedEffort(0.0, Complexity.TRIVIAL),
                blockers = emptyList(),
                highRiskSystems = emptyList()
            ),
            executionTime = "PT0S", // Will be updated
            timestamp = Instant.now().toString()
        )
        
        // Generate deployment recommendation
        val deploymentDecision = generateDeploymentRecommendation(preliminaryResult)
        
        // Return final result
        AuditResult(
            phaseResults = phaseResults,
            phaseScores = phaseScores,
            launchReadinessScore = launchReadinessScore,
            startupQualityScore = startupQualityScore,
            deploymentDecision = deploymentDecision,
            executionTime = "PT0S", // Will be updated by caller
            timestamp = Instant.now().toString()
        )
    }
    
    /**
     * Builds the shared AuditContext by scanning the project structure.
     */
    private fun buildAuditContext(config: AuditConfig): AuditContext {
        val projectRoot = config.projectRoot
        
        // Scan for Kotlin source files
        val sourceFiles = scanKotlinFiles(projectRoot)
        
        // Scan for build files
        val buildFiles = scanBuildFiles(projectRoot)
        
        // Build module structure (simplified for now)
        val moduleStructure = buildModuleGraph(projectRoot, sourceFiles)
        
        // Build dependency graph (placeholder for now)
        val dependencies = DependencyGraph(
            nodes = emptyList(),
            edges = emptyMap()
        )
        
        return AuditContext(
            projectRoot = projectRoot,
            sourceFiles = sourceFiles,
            buildFiles = buildFiles,
            moduleStructure = moduleStructure,
            dependencies = dependencies,
            phaseResults = mutableMapOf()
        )
    }
    
    /**
     * Resolves the execution order of phases based on dependencies.
     * 
     * Phase dependencies:
     * - Phases 1-2 can run in parallel (no dependencies)
     * - Phase 3 depends on Phase 2 (needs API integration info)
     * - Phase 4 can run independently
     * - Phase 5 depends on Phase 3 (needs architecture info)
     * - Phase 6 can run independently
     * - Phase 7 depends on Phases 3 and 6 (needs architecture and security info)
     * - Phase 8 depends on all previous phases
     * 
     * For simplicity, we execute sequentially in order 1-8.
     * Future optimization: parallel execution of independent phases.
     */
    private fun resolvePhaseExecutionOrder(enabledPhases: Set<Int>): List<Int> {
        return enabledPhases.sorted()
    }
    
    /**
     * Calculates the launch readiness score (1-10) based on phase scores.
     * 
     * Weighting:
     * - Phase 1 (Real Data): 15%
     * - Phase 2 (API Integration): 15%
     * - Phase 3 (Architecture): 15%
     * - Phase 4 (UI/UX): 10%
     * - Phase 5 (Performance): 10%
     * - Phase 6 (Security): 20% (highest weight)
     * - Phase 7 (Deployment): 15%
     * - Phase 8 (Startup Quality): Not included in launch readiness
     */
    private fun calculateLaunchReadinessScore(
        phaseScores: Map<Int, PhaseScore>,
        thresholds: ThresholdConfig
    ): Double {
        val weights = mapOf(
            1 to 0.15,  // Real Data
            2 to 0.15,  // API Integration
            3 to 0.15,  // Architecture
            4 to 0.10,  // UI/UX
            5 to 0.10,  // Performance
            6 to 0.20,  // Security (highest weight)
            7 to 0.15   // Deployment
        )
        
        var weightedSum = 0.0
        var totalWeight = 0.0
        
        for ((phase, weight) in weights) {
            phaseScores[phase]?.let { score ->
                weightedSum += score.score * weight
                totalWeight += weight
            }
        }
        
        return if (totalWeight > 0) {
            (weightedSum / totalWeight).coerceIn(1.0, 10.0)
        } else {
            1.0 // Minimum score if no phases executed
        }
    }
    
    /**
     * Calculates the startup quality score (1-10) based primarily on Phase 8 results.
     */
    private fun calculateStartupQualityScore(phaseScores: Map<Int, PhaseScore>): Double {
        // Phase 8 is the Startup Quality Evaluator
        val phase8Score = phaseScores[8]?.score ?: 1.0
        
        // Also consider other phases for holistic quality assessment
        val otherPhasesAvg = phaseScores
            .filterKeys { it != 8 }
            .values
            .map { it.score }
            .average()
            .takeIf { !it.isNaN() } ?: 1.0
        
        // Weight Phase 8 heavily (70%) but consider others (30%)
        val score = (phase8Score * 0.7) + (otherPhasesAvg * 0.3)
        
        return score.coerceIn(1.0, 10.0)
    }
    
    /**
     * Recursively scans for Kotlin source files.
     */
    private fun scanKotlinFiles(root: File): List<File> {
        val kotlinFiles = mutableListOf<File>()
        
        fun scan(dir: File) {
            if (!dir.exists() || !dir.isDirectory) return
            
            dir.listFiles()?.forEach { file ->
                when {
                    file.isDirectory && !file.name.startsWith(".") -> scan(file)
                    file.extension == "kt" -> kotlinFiles.add(file)
                }
            }
        }
        
        scan(root)
        return kotlinFiles
    }
    
    /**
     * Scans for Gradle build files.
     */
    private fun scanBuildFiles(root: File): List<File> {
        val buildFiles = mutableListOf<File>()
        
        fun scan(dir: File) {
            if (!dir.exists() || !dir.isDirectory) return
            
            dir.listFiles()?.forEach { file ->
                when {
                    file.isDirectory && !file.name.startsWith(".") -> scan(file)
                    file.name.endsWith("build.gradle.kts") || file.name.endsWith("build.gradle") -> {
                        buildFiles.add(file)
                    }
                }
            }
        }
        
        scan(root)
        return buildFiles
    }
    
    /**
     * Builds a simplified module graph from project structure.
     */
    private fun buildModuleGraph(projectRoot: File, sourceFiles: List<File>): ModuleGraph {
        val modules = mutableListOf<Module>()
        
        // Group source files by module directory
        val moduleMap = sourceFiles
            .groupBy { file -> findModuleRoot(file, projectRoot) }
            .filterKeys { it != null }
        
        for ((moduleRoot, files) in moduleMap) {
            if (moduleRoot == null) continue
            
            val moduleName = moduleRoot.name
            val moduleType = inferModuleType(moduleName)
            
            modules.add(
                Module(
                    name = moduleName,
                    type = moduleType,
                    path = moduleRoot,
                    sourceFiles = files
                )
            )
        }
        
        // For now, simplified dependency graph (no actual parsing of dependencies)
        return ModuleGraph(
            modules = modules,
            dependencies = emptyMap()
        )
    }
    
    /**
     * Finds the module root directory for a source file.
     */
    private fun findModuleRoot(file: File, projectRoot: File): File? {
        var current = file.parentFile
        
        while (current != null && current != projectRoot) {
            // Check if this directory contains a build.gradle.kts file
            if (File(current, "build.gradle.kts").exists() || 
                File(current, "build.gradle").exists()) {
                return current
            }
            current = current.parentFile
        }
        
        return null
    }
    
    /**
     * Infers module type from module name.
     */
    private fun inferModuleType(moduleName: String): ModuleType {
        return when {
            moduleName == "app" -> ModuleType.APP
            moduleName.startsWith("feature-") || moduleName.contains("feature") -> ModuleType.FEATURE
            moduleName.startsWith("core-") || moduleName.contains("core") -> ModuleType.CORE
            moduleName.startsWith("data-") || moduleName.contains("data") -> ModuleType.DATA
            moduleName.startsWith("domain-") || moduleName.contains("domain") -> ModuleType.DOMAIN
            else -> ModuleType.CORE
        }
    }
}

/**
 * Extension to convert Duration to ISO-8601 string format.
 */
private fun Duration.toIsoString(): String {
    return this.toIsoString()
}
