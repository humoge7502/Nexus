package com.nexus.audit.core.types

import java.io.File

/**
 * Shared context containing project information and findings from previous phases.
 * 
 * Validates: Requirements 9.1, 10.1
 */
data class AuditContext(
    val projectRoot: File,
    val sourceFiles: List<File>,
    val buildFiles: List<File>,
    val moduleStructure: ModuleGraph,
    val dependencies: DependencyGraph,
    val phaseResults: MutableMap<Int, PhaseResult> = mutableMapOf()
)

/**
 * Dependency graph representing module relationships.
 */
data class DependencyGraph(
    val nodes: List<DependencyNode>,
    val edges: Map<DependencyNode, Set<DependencyNode>>
)

/**
 * A single dependency node in the graph.
 */
data class DependencyNode(
    val groupId: String,
    val artifactId: String,
    val version: String
)

/**
 * Module structure graph.
 */
data class ModuleGraph(
    val modules: List<Module>,
    val dependencies: Map<Module, Set<Module>>
)

/**
 * Represents a single module in the project.
 */
data class Module(
    val name: String,
    val type: ModuleType,
    val path: File,
    val sourceFiles: List<File>
)

/**
 * Types of modules in the project.
 */
enum class ModuleType {
    APP,
    FEATURE,
    CORE,
    DATA,
    DOMAIN,
    PRESENTATION
}
