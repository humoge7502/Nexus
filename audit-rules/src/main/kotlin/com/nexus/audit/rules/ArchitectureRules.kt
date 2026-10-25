package com.nexus.audit.rules

import com.nexus.audit.core.types.Severity

/**
 * Architecture & layering rules (Phase 3).
 */
object ArchitectureRules {
    val LAYERING_VIOLATION = Rule(
        id = "ARCH-001",
        title = "Feature module depends on another feature module",
        category = "Architecture",
        severity = Severity.HIGH,
        description = "A :feature module declares project(\":feature:...\") on another feature module, " +
            "violating clean modular layering. Features must depend only on :core modules.",
        remediation = "Route shared behaviour through a :core module (domain/data) and remove the " +
            "feature-to-feature dependency."
    )

    val OVERSIZED_FILE = Rule(
        id = "ARCH-002",
        title = "Source file exceeds maintainability threshold",
        category = "Architecture",
        severity = Severity.MEDIUM,
        description = "A Kotlin file is larger than 600 lines, which correlates with lower " +
            "review velocity and higher defect density.",
        remediation = "Split into smaller cohesive units; extract composables/use-cases/repositories."
    )

    val MAIN_THREAD_BLOCKING = Rule(
        id = "ARCH-003",
        title = "runBlocking used on a production code path",
        category = "Architecture",
        severity = Severity.HIGH,
        description = "runBlocking blocks the calling thread. On a dispatcher or main thread it can " +
            "cause jank or ANR.",
        remediation = "Convert the call site to suspend and use a coroutine scope."
    )

    init {
        listOf(LAYERING_VIOLATION, OVERSIZED_FILE, MAIN_THREAD_BLOCKING).forEach(RuleCatalog::register)
    }
}
