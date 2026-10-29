package com.nexus.audit.core.types

import kotlinx.serialization.Serializable
import kotlin.time.Duration

/**
 * Aggregated audit results from all phases.
 * 
 * Validates: Requirements 9.1, 9.14, 9.15, 11.1-11.8
 */
@Serializable
data class AuditResult(
    val phaseResults: Map<Int, PhaseResult>,
    val phaseScores: Map<Int, PhaseScore>,
    val launchReadinessScore: Double, // 1.0 to 10.0
    val startupQualityScore: Double, // 1.0 to 10.0
    val deploymentDecision: DeploymentDecision,
    val executionTime: String, // Duration as ISO-8601 string
    val timestamp: String // ISO-8601 timestamp
)

/**
 * Final deployment recommendation.
 * 
 * Validates: Requirement 11.1-11.4
 * 
 * CRITICAL RULES:
 * 1. If ANY Deployment_Blocker exists, MUST be "Not Ready" with no exceptions
 * 2. 'Ready' recommendations ARE allowed regardless of Launch_Readiness_Score value
 * 3. Launch_Readiness_Score below threshold is a factor but not an absolute blocker
 * 4. Deployment blockers have absolute veto power over deployment decisions
 */
@Serializable
data class DeploymentDecision(
    val recommendation: DeploymentRecommendation,
    val reasoning: String,
    val topPriorities: List<String>,
    val estimatedEffort: EstimatedEffort,
    val blockers: List<String>,
    val highRiskSystems: List<String>
)

/**
 * Deployment recommendation levels.
 */
@Serializable
enum class DeploymentRecommendation {
    READY,              // Safe to deploy
    READY_WITH_RISKS,   // Can deploy but with monitored risks
    NOT_READY           // Deployment blockers exist
}

/**
 * Estimated effort to address issues.
 */
@Serializable
data class EstimatedEffort(
    val developerDays: Double,
    val complexity: Complexity
)

/**
 * Complexity levels for effort estimation.
 */
@Serializable
enum class Complexity {
    TRIVIAL,
    SIMPLE,
    MODERATE,
    COMPLEX,
    VERY_COMPLEX
}
