package com.nexus.audit.scanner

import com.nexus.audit.core.types.Module
import com.nexus.audit.core.types.ModuleGraph
import com.nexus.audit.core.types.ModuleType
import java.io.File

/**
 * Scans the project's file system to identify Kotlin files, build files, resources,
 * and constructs the module dependency graph.
 * 
 * Validates: Requirements 10.1, 10.2
 */
interface FileSystemScanner {
    /**
     * Recursively scans project for all Kotlin source files (.kt extension).
     * 
     * @param projectRoot The root directory of the project
     * @return List of all .kt files found in the project
     */
    fun scanKotlinFiles(projectRoot: File): List<File>
    
    /**
     * Scans for Gradle build files (build.gradle.kts).
     * 
     * @param projectRoot The root directory of the project
     * @return List of all build.gradle.kts files found
     */
    fun scanBuildFiles(projectRoot: File): List<File>
    
    /**
     * Scans for Android resource directories and files.
     * 
     * @param projectRoot The root directory of the project
     * @return List of all resource files (res/ directories and their contents)
     */
    fun scanResources(projectRoot: File): List<File>
    
    /**
     * Builds the module dependency graph by parsing module structure
     * and analyzing build.gradle.kts dependencies.
     * 
     * @param projectRoot The root directory of the project
     * @return ModuleGraph representing module structure and inter-module dependencies
     */
    fun buildModuleGraph(projectRoot: File): ModuleGraph
}

/**
 * Default implementation of FileSystemScanner.
 */
class DefaultFileSystemScanner : FileSystemScanner {
    
    companion object {
        private val EXCLUDED_DIRS = setOf(
            "build",
            ".gradle",
            ".idea",
            ".git",
            "node_modules",
            ".kotlin"
        )
        
        private const val KOTLIN_EXTENSION = ".kt"
        private const val BUILD_FILE_NAME = "build.gradle.kts"
        private const val RESOURCE_DIR_NAME = "res"
    }
    
    override fun scanKotlinFiles(projectRoot: File): List<File> {
        require(projectRoot.exists() && projectRoot.isDirectory) {
            "Project root must be an existing directory: ${projectRoot.absolutePath}"
        }
        
        return scanForFiles(projectRoot) { file ->
            file.isFile && file.name.endsWith(KOTLIN_EXTENSION)
        }
    }
    
    override fun scanBuildFiles(projectRoot: File): List<File> {
        require(projectRoot.exists() && projectRoot.isDirectory) {
            "Project root must be an existing directory: ${projectRoot.absolutePath}"
        }
        
        return scanForFiles(projectRoot) { file ->
            file.isFile && file.name == BUILD_FILE_NAME
        }
    }
    
    override fun scanResources(projectRoot: File): List<File> {
        require(projectRoot.exists() && projectRoot.isDirectory) {
            "Project root must be an existing directory: ${projectRoot.absolutePath}"
        }
        
        val resourceDirs = mutableListOf<File>()
        val resourceFiles = mutableListOf<File>()
        
        // Find all res/ directories first
        findResourceDirectories(projectRoot, resourceDirs)
        
        // Collect all files within res/ directories
        resourceDirs.forEach { resDir ->
            resourceFiles.addAll(scanForFiles(resDir) { it.isFile })
        }
        
        return resourceFiles
    }
    
    override fun buildModuleGraph(projectRoot: File): ModuleGraph {
        require(projectRoot.exists() && projectRoot.isDirectory) {
            "Project root must be an existing directory: ${projectRoot.absolutePath}"
        }
        
        val buildFiles = scanBuildFiles(projectRoot)
        val modules = mutableListOf<Module>()
        val moduleDependencies = mutableMapOf<Module, Set<Module>>()
        
        // Build module list
        buildFiles.forEach { buildFile ->
            val moduleDir = buildFile.parentFile
            val module = createModule(moduleDir, projectRoot)
            modules.add(module)
        }
        
        // Parse dependencies from build files
        modules.forEach { module ->
            val buildFile = File(module.path, BUILD_FILE_NAME)
            if (buildFile.exists()) {
                val dependencies = parseDependencies(buildFile, modules)
                moduleDependencies[module] = dependencies
            } else {
                moduleDependencies[module] = emptySet()
            }
        }
        
        return ModuleGraph(
            modules = modules,
            dependencies = moduleDependencies
        )
    }
    
    /**
     * Recursively scans for files matching the given predicate.
     */
    private fun scanForFiles(directory: File, predicate: (File) -> Boolean): List<File> {
        val results = mutableListOf<File>()
        
        if (!directory.exists() || !directory.isDirectory) {
            return results
        }
        
        directory.listFiles()?.forEach { file ->
            when {
                file.isFile && predicate(file) -> {
                    results.add(file)
                }
                file.isDirectory && !shouldExcludeDirectory(file) -> {
                    results.addAll(scanForFiles(file, predicate))
                }
            }
        }
        
        return results
    }
    
    /**
     * Finds all res/ directories in the project.
     */
    private fun findResourceDirectories(directory: File, results: MutableList<File>) {
        if (!directory.exists() || !directory.isDirectory) {
            return
        }
        
        directory.listFiles()?.forEach { file ->
            when {
                file.isDirectory && file.name == RESOURCE_DIR_NAME -> {
                    results.add(file)
                }
                file.isDirectory && !shouldExcludeDirectory(file) -> {
                    findResourceDirectories(file, results)
                }
            }
        }
    }
    
    /**
     * Determines if a directory should be excluded from scanning.
     */
    private fun shouldExcludeDirectory(directory: File): Boolean {
        return EXCLUDED_DIRS.contains(directory.name)
    }
    
    /**
     * Creates a Module from a module directory.
     */
    private fun createModule(moduleDir: File, projectRoot: File): Module {
        val relativePath = moduleDir.relativeTo(projectRoot).path
        val moduleName = if (relativePath.isEmpty()) "root" else relativePath.replace(File.separator, ":")
        
        val moduleType = inferModuleType(moduleName, moduleDir)
        val sourceFiles = scanKotlinFiles(moduleDir)
        
        return Module(
            name = moduleName,
            type = moduleType,
            path = moduleDir,
            sourceFiles = sourceFiles
        )
    }
    
    /**
     * Infers the module type from its name and location.
     */
    private fun inferModuleType(moduleName: String, moduleDir: File): ModuleType {
        return when {
            moduleName == "root" || moduleName == "app" -> ModuleType.APP
            moduleName.contains("core") -> ModuleType.CORE
            moduleName.contains("feature") -> ModuleType.FEATURE
            moduleName.contains("data") -> ModuleType.DATA
            moduleName.contains("domain") -> ModuleType.DOMAIN
            moduleName.contains("presentation") -> ModuleType.PRESENTATION
            else -> {
                // Check parent directory structure for hints
                val parentPath = moduleDir.parentFile?.name ?: ""
                when {
                    parentPath.contains("core") -> ModuleType.CORE
                    parentPath.contains("feature") -> ModuleType.FEATURE
                    else -> ModuleType.FEATURE // Default to FEATURE
                }
            }
        }
    }
    
    /**
     * Parses module dependencies from a build.gradle.kts file.
     * 
     * Looks for project(...) declarations in the dependencies block.
     */
    private fun parseDependencies(buildFile: File, allModules: List<Module>): Set<Module> {
        val dependencies = mutableSetOf<Module>()
        
        try {
            val buildContent = buildFile.readText()
            
            // Find all project(...) dependencies
            // Pattern: project(":module:name") or project(':module:name')
            val projectDepPattern = """project\(["']([^"']+)["']\)""".toRegex()
            
            projectDepPattern.findAll(buildContent).forEach { match ->
                val dependencyPath = match.groupValues[1]
                
                // Find the matching module
                val dependencyModule = allModules.find { module ->
                    module.name == dependencyPath || 
                    module.name == dependencyPath.removePrefix(":")
                }
                
                dependencyModule?.let { dependencies.add(it) }
            }
        } catch (e: Exception) {
            // If we can't parse the build file, return empty dependencies
            // This is not critical for the scanner operation
        }
        
        return dependencies
    }
}
