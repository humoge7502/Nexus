package com.nexus.audit.core.types

import kotlinx.serialization.Serializable

/**
 * Base sealed class for phase-specific results.
 * 
 * Validates: Requirements 9.1, 9.2-9.8
 */
@Serializable
sealed class PhaseResult {
    abstract val phaseNumber: Int
    abstract val findings: List<Finding>
    abstract val metadata: Map<String, String>
}

/**
 * Generic phase result implementation for testing and simple cases.
 */
@Serializable
data class GenericPhaseResult(
    override val phaseNumber: Int,
    override val findings: List<Finding>,
    override val metadata: Map<String, String> = emptyMap()
) : PhaseResult()

/**
 * Score for a specific phase (1-10).
 */
@Serializable
data class PhaseScore(
    val phaseNumber: Int,
    val phaseName: String,
    val score: Double, // 1.0 to 10.0
    val reasoning: String
)
