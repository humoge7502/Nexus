package com.nexus.audit.core.types

import kotlinx.serialization.Serializable
import java.io.File

/**
 * Represents a single audit finding.
 * 
 * Validates: Requirements 9.1, 9.2-9.15
 */
@Serializable
data class Finding(
    val severity: Severity,
    val category: String,
    val title: String,
    val description: String,
    val location: CodeLocation? = null,
    val recommendation: String,
    val requirement: String? = null // References requirement ID from requirements.md
)

/**
 * Severity levels for audit findings.
 */
@Serializable
enum class Severity {
    CRITICAL,      // Deployment blocker
    HIGH,          // High-risk issue
    MEDIUM,        // Should fix before launch
    LOW,           // Nice to have
    INFO           // Informational finding
}

/**
 * Location in source code where an issue was found.
 */
@Serializable
data class CodeLocation(
    @Serializable(with = FileSerializer::class)
    val file: File,
    val line: Int? = null,
    val column: Int? = null,
    val snippet: String? = null
)
