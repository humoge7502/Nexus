package com.nexus.audit.core.types

import kotlinx.serialization.Serializable
import java.io.File

/**
 * Configuration for the production-readiness audit system.
 * 
 * Validates: Requirements 9.1, 10.1, 12.1
 */
@Serializable
data class AuditConfig(
    @Serializable(with = FileSerializer::class)
    val projectRoot: File,
    val enabledPhases: Set<Int> = (1..8).toSet(),
    val thresholds: ThresholdConfig = ThresholdConfig(),
    val outputFormat: Set<OutputFormat> = setOf(OutputFormat.MARKDOWN),
    @Serializable(with = FileSerializer::class)
    val reportOutputDir: File
)

/**
 * Threshold configuration for audit validation.
 */
@Serializable
data class ThresholdConfig(
    val minLaunchReadinessScore: Int = 7,
    val maxCriticalIssues: Int = 0,
    val maxHighRiskIssues: Int = 5,
    val minTestCoverage: Double = 0.70,
    val maxColdStartupMs: Long = 2000,
    val maxMemoryLeakCount: Int = 0
)

/**
 * Output format for audit reports.
 */
@Serializable
enum class OutputFormat {
    MARKDOWN,
    JSON,
    HTML
}
