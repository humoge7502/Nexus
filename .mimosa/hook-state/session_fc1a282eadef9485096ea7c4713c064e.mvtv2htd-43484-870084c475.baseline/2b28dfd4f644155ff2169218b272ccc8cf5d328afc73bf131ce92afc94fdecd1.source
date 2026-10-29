package com.nexus.audit.core.analyzer

import com.nexus.audit.core.types.AuditContext
import com.nexus.audit.core.types.PhaseResult
import com.nexus.audit.core.types.PhaseScore

/**
 * Base interface for all phase analyzers.
 * Each of the 8 audit phases implements this interface.
 * 
 * Validates: Requirements 9.1, 10.1
 */
interface PhaseAnalyzer {
    /**
     * Phase number (1-8).
     */
    val phaseNumber: Int
    
    /**
     * Human-readable phase name.
     */
    val phaseName: String
    
    /**
     * Executes this phase's analysis.
     * 
     * @param context Shared context containing project info and findings from previous phases
     * @return Phase-specific findings
     */
    suspend fun analyze(context: AuditContext): PhaseResult
    
    /**
     * Calculates phase-specific score (1-10).
     * 
     * @param result The phase result to score
     * @return Score from 1.0 to 10.0 with reasoning
     */
    fun calculateScore(result: PhaseResult): PhaseScore
}
