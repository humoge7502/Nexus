package com.nexus.audit.rules

import com.nexus.audit.core.types.Severity

/**
 * A single auditable rule. The [id] is referenced by phase analyzers so that
 * every emitted [com.nexus.audit.core.types.Finding] can be traced back to the
 * rule that produced it (traceability for requirements 9.x / 10.x / 11.x).
 */
data class Rule(
    val id: String,
    val title: String,
    val category: String,
    val severity: Severity,
    val description: String,
    val remediation: String,
)

/**
 * Catalog of every rule the audit system knows about. Analyzers look rules up
 * by [Rule.id] and emit a [com.nexus.audit.core.types.Finding] with the same
 * [com.nexus.audit.core.types.Finding.requirement] value.
 */
object RuleCatalog {
    private val all: MutableList<Rule> = mutableListOf()

    operator fun get(id: String): Rule =
        all.firstOrNull { it.id == id }
            ?: Rule(id, id, "UNKNOWN", Severity.INFO, "Rule not registered.", "Register the rule.")

    fun register(rule: Rule) {
        if (all.none { it.id == rule.id }) all.add(rule)
    }

    fun all(): List<Rule> = all.toList()
}
