package com.nexus.audit.scanner

import com.nexus.audit.core.types.ModuleType
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

/**
 * Unit tests for FileSystemScanner implementation.
 * 
 * Validates: Requirements 10.1, 10.2
 */
class FileSystemScannerTest {
    
    private lateinit var tempDir: File
    private lateinit var scanner: FileSystemScanner
    
    @Before
    fun setup() {
        tempDir = createTempDir("scanner-test")
        scanner = DefaultFileSystemScanner()
    }
    
    @After
    fun cleanup() {
        tempDir.deleteRecursively()
    }
    
    // scanKotlinFiles tests
    
    @Test
    fun `scanKotlinFiles finds all Kotlin files recursively`() {
        // Given
        val srcDir = File(tempDir, "src/main/kotlin/com/example")
        srcDir.mkdirs()
        
        File(srcDir, "MainActivity.kt").writeText("class MainActivity")
        File(srcDir, "App.kt").writeText("class App")
        
        val nestedDir = File(srcDir, "ui")
        nestedDir.mkdirs()
        File(nestedDir, "HomeScreen.kt").writeText("class HomeScreen")
        
        // When
        val kotlinFiles = scanner.scanKotlinFiles(tempDir)
        
        // Then
        assertEquals(3, kotlinFiles.size)
        assertTrue(kotlinFiles.any { it.name == "MainActivity.kt" })
        assertTrue(kotlinFiles.any { it.name == "App.kt" })
        assertTrue(kotlinFiles.any { it.name == "HomeScreen.kt" })
    }
    
    @Test
    fun `scanKotlinFiles ignores non-Kotlin files`() {
        // Given
        val srcDir = File(tempDir, "src")
        srcDir.mkdirs()
        
        File(srcDir, "Test.kt").writeText("class Test")
        File(srcDir, "README.md").writeText("# README")
        File(srcDir, "config.json").writeText("{}")
        File(srcDir, "script.sh").writeText("#!/bin/bash")
        
        // When
        val kotlinFiles = scanner.scanKotlinFiles(tempDir)
        
        // Then
        assertEquals(1, kotlinFiles.size)
        assertEquals("Test.kt", kotlinFiles.first().name)
    }
    
    @Test
    fun `scanKotlinFiles excludes build directories`() {
        // Given
        val srcDir = File(tempDir, "src")
        srcDir.mkdirs()
        File(srcDir, "Source.kt").writeText("class Source")
        
        val buildDir = File(tempDir, "build")
        buildDir.mkdirs()
        File(buildDir, "Generated.kt").writeText("class Generated")
        
        // When
        val kotlinFiles = scanner.scanKotlinFiles(tempDir)
        
        // Then
        assertEquals(1, kotlinFiles.size)
        assertEquals("Source.kt", kotlinFiles.first().name)
    }
    
    @Test
    fun `scanKotlinFiles excludes gradle directories`() {
        // Given
        val srcDir = File(tempDir, "src")
        srcDir.mkdirs()
        File(srcDir, "Source.kt").writeText("class Source")
        
        val gradleDir = File(tempDir, ".gradle")
        gradleDir.mkdirs()
        File(gradleDir, "Cache.kt").writeText("class Cache")
        
        // When
        val kotlinFiles = scanner.scanKotlinFiles(tempDir)
        
        // Then
        assertEquals(1, kotlinFiles.size)
        assertEquals("Source.kt", kotlinFiles.first().name)
    }
    
    @Test
    fun `scanKotlinFiles returns empty list for empty project`() {
        // Given
        val emptyDir = File(tempDir, "empty")
        emptyDir.mkdirs()
        
        // When
        val kotlinFiles = scanner.scanKotlinFiles(emptyDir)
        
        // Then
        assertTrue(kotlinFiles.isEmpty())
    }
    
    @Test
    fun `scanKotlinFiles throws exception for non-existent directory`() {
        // Given
        val nonExistent = File(tempDir, "does-not-exist")
        
        // When/Then
        assertFailsWith<IllegalArgumentException> {
            scanner.scanKotlinFiles(nonExistent)
        }
    }
    
    // scanBuildFiles tests
    
    @Test
    fun `scanBuildFiles finds all build gradle files`() {
        // Given
        File(tempDir, "build.gradle.kts").writeText("plugins { }")
        
        val appModule = File(tempDir, "app")
        appModule.mkdirs()
        File(appModule, "build.gradle.kts").writeText("plugins { }")
        
        val coreModule = File(tempDir, "core/data")
        coreModule.mkdirs()
        File(coreModule, "build.gradle.kts").writeText("plugins { }")
        
        // When
        val buildFiles = scanner.scanBuildFiles(tempDir)
        
        // Then
        assertEquals(3, buildFiles.size)
        assertTrue(buildFiles.all { it.name == "build.gradle.kts" })
    }
    
    @Test
    fun `scanBuildFiles ignores other gradle files`() {
        // Given
        File(tempDir, "build.gradle.kts").writeText("plugins { }")
        File(tempDir, "settings.gradle.kts").writeText("rootProject.name")
        File(tempDir, "gradle.properties").writeText("org.gradle")
        
        // When
        val buildFiles = scanner.scanBuildFiles(tempDir)
        
        // Then
        assertEquals(1, buildFiles.size)
        assertEquals("build.gradle.kts", buildFiles.first().name)
    }
    
    @Test
    fun `scanBuildFiles excludes build directories`() {
        // Given
        File(tempDir, "build.gradle.kts").writeText("plugins { }")
        
        val buildDir = File(tempDir, "build")
        buildDir.mkdirs()
        File(buildDir, "build.gradle.kts").writeText("plugins { }")
        
        // When
        val buildFiles = scanner.scanBuildFiles(tempDir)
        
        // Then
        assertEquals(1, buildFiles.size)
        assertEquals(tempDir, buildFiles.first().parentFile)
    }
    
    // scanResources tests
    
    @Test
    fun `scanResources finds all resource files`() {
        // Given
        val resDir = File(tempDir, "app/src/main/res")
        
        val drawableDir = File(resDir, "drawable")
        drawableDir.mkdirs()
        File(drawableDir, "icon.xml").writeText("<vector>")
        
        val valuesDir = File(resDir, "values")
        valuesDir.mkdirs()
        File(valuesDir, "strings.xml").writeText("<resources>")
        File(valuesDir, "colors.xml").writeText("<resources>")
        
        // When
        val resourceFiles = scanner.scanResources(tempDir)
        
        // Then
        assertEquals(3, resourceFiles.size)
        assertTrue(resourceFiles.any { it.name == "icon.xml" })
        assertTrue(resourceFiles.any { it.name == "strings.xml" })
        assertTrue(resourceFiles.any { it.name == "colors.xml" })
    }
    
    @Test
    fun `scanResources finds resources in multiple modules`() {
        // Given
        val appRes = File(tempDir, "app/src/main/res/values")
        appRes.mkdirs()
        File(appRes, "strings.xml").writeText("<resources>")
        
        val featureRes = File(tempDir, "feature/auth/src/main/res/values")
        featureRes.mkdirs()
        File(featureRes, "strings.xml").writeText("<resources>")
        
        // When
        val resourceFiles = scanner.scanResources(tempDir)
        
        // Then
        assertEquals(2, resourceFiles.size)
        assertTrue(resourceFiles.all { it.name == "strings.xml" })
    }
    
    @Test
    fun `scanResources returns empty list when no resources exist`() {
        // Given - no res directories
        val srcDir = File(tempDir, "src")
        srcDir.mkdirs()
        
        // When
        val resourceFiles = scanner.scanResources(tempDir)
        
        // Then
        assertTrue(resourceFiles.isEmpty())
    }
    
    @Test
    fun `scanResources excludes build directory resources`() {
        // Given
        val mainRes = File(tempDir, "app/src/main/res/values")
        mainRes.mkdirs()
        File(mainRes, "strings.xml").writeText("<resources>")
        
        val buildRes = File(tempDir, "build/res/values")
        buildRes.mkdirs()
        File(buildRes, "generated.xml").writeText("<resources>")
        
        // When
        val resourceFiles = scanner.scanResources(tempDir)
        
        // Then
        assertEquals(1, resourceFiles.size)
        assertEquals("strings.xml", resourceFiles.first().name)
    }
    
    // buildModuleGraph tests
    
    @Test
    fun `buildModuleGraph creates graph with single module`() {
        // Given
        File(tempDir, "build.gradle.kts").writeText("plugins { kotlin(\"jvm\") }")
        
        val srcDir = File(tempDir, "src/main/kotlin")
        srcDir.mkdirs()
        File(srcDir, "Main.kt").writeText("fun main() {}")
        
        // When
        val graph = scanner.buildModuleGraph(tempDir)
        
        // Then
        assertEquals(1, graph.modules.size)
        assertEquals("root", graph.modules.first().name)
        assertEquals(ModuleType.APP, graph.modules.first().type)
        assertEquals(1, graph.modules.first().sourceFiles.size)
    }
    
    @Test
    fun `buildModuleGraph creates graph with multiple modules`() {
        // Given
        File(tempDir, "build.gradle.kts").writeText("plugins { }")
        
        val appModule = File(tempDir, "app")
        appModule.mkdirs()
        File(appModule, "build.gradle.kts").writeText("plugins { }")
        
        val coreModule = File(tempDir, "core/common")
        coreModule.mkdirs()
        File(coreModule, "build.gradle.kts").writeText("plugins { }")
        
        // When
        val graph = scanner.buildModuleGraph(tempDir)
        
        // Then
        assertEquals(3, graph.modules.size)
        assertTrue(graph.modules.any { it.name == "root" })
        assertTrue(graph.modules.any { it.name == "app" })
        assertTrue(graph.modules.any { it.name.contains("core") })
    }
    
    @Test
    fun `buildModuleGraph infers correct module types`() {
        // Given
        File(tempDir, "build.gradle.kts").writeText("plugins { }")
        
        val appModule = File(tempDir, "app")
        appModule.mkdirs()
        File(appModule, "build.gradle.kts").writeText("plugins { }")
        
        val coreModule = File(tempDir, "core/common")
        coreModule.mkdirs()
        File(coreModule, "build.gradle.kts").writeText("plugins { }")
        
        val featureModule = File(tempDir, "feature/auth")
        featureModule.mkdirs()
        File(featureModule, "build.gradle.kts").writeText("plugins { }")
        
        // When
        val graph = scanner.buildModuleGraph(tempDir)
        
        // Then
        val rootModule = graph.modules.find { it.name == "root" }
        assertEquals(ModuleType.APP, rootModule?.type)
        
        val app = graph.modules.find { it.name == "app" }
        assertEquals(ModuleType.APP, app?.type)
        
        val core = graph.modules.find { it.name.contains("core") }
        assertEquals(ModuleType.CORE, core?.type)
        
        val feature = graph.modules.find { it.name.contains("feature") }
        assertEquals(ModuleType.FEATURE, feature?.type)
    }
    
    @Test
    fun `buildModuleGraph parses project dependencies`() {
        // Given
        File(tempDir, "build.gradle.kts").writeText("plugins { }")
        
        val coreModule = File(tempDir, "core")
        coreModule.mkdirs()
        File(coreModule, "build.gradle.kts").writeText("plugins { }")
        
        val appModule = File(tempDir, "app")
        appModule.mkdirs()
        File(appModule, "build.gradle.kts").writeText("""
            dependencies {
                implementation(project(":core"))
            }
        """.trimIndent())
        
        // When
        val graph = scanner.buildModuleGraph(tempDir)
        
        // Then
        val app = graph.modules.find { it.name == "app" }!!
        val core = graph.modules.find { it.name == "core" }!!
        
        val appDeps = graph.dependencies[app]
        assertEquals(1, appDeps?.size)
        assertTrue(appDeps?.contains(core) == true)
    }
    
    @Test
    fun `buildModuleGraph handles modules with no dependencies`() {
        // Given
        File(tempDir, "build.gradle.kts").writeText("plugins { }")
        
        val coreModule = File(tempDir, "core")
        coreModule.mkdirs()
        File(coreModule, "build.gradle.kts").writeText("plugins { }")
        
        // When
        val graph = scanner.buildModuleGraph(tempDir)
        
        // Then
        val core = graph.modules.find { it.name == "core" }!!
        val coreDeps = graph.dependencies[core]
        assertTrue(coreDeps?.isEmpty() == true)
    }
    
    @Test
    fun `buildModuleGraph handles complex dependency chains`() {
        // Given
        File(tempDir, "build.gradle.kts").writeText("plugins { }")
        
        val dataModule = File(tempDir, "data")
        dataModule.mkdirs()
        File(dataModule, "build.gradle.kts").writeText("plugins { }")
        
        val domainModule = File(tempDir, "domain")
        domainModule.mkdirs()
        File(domainModule, "build.gradle.kts").writeText("""
            dependencies {
                implementation(project(":data"))
            }
        """.trimIndent())
        
        val appModule = File(tempDir, "app")
        appModule.mkdirs()
        File(appModule, "build.gradle.kts").writeText("""
            dependencies {
                implementation(project(":domain"))
                implementation(project(":data"))
            }
        """.trimIndent())
        
        // When
        val graph = scanner.buildModuleGraph(tempDir)
        
        // Then
        val app = graph.modules.find { it.name == "app" }!!
        val domain = graph.modules.find { it.name == "domain" }!!
        val data = graph.modules.find { it.name == "data" }!!
        
        val appDeps = graph.dependencies[app]
        assertEquals(2, appDeps?.size)
        assertTrue(appDeps?.contains(domain) == true)
        assertTrue(appDeps?.contains(data) == true)
        
        val domainDeps = graph.dependencies[domain]
        assertEquals(1, domainDeps?.size)
        assertTrue(domainDeps?.contains(data) == true)
    }
    
    @Test
    fun `buildModuleGraph throws exception for non-existent directory`() {
        // Given
        val nonExistent = File(tempDir, "does-not-exist")
        
        // When/Then
        assertFailsWith<IllegalArgumentException> {
            scanner.buildModuleGraph(nonExistent)
        }
    }
    
    // Edge cases
    
    @Test
    fun `scanner handles deeply nested directory structures`() {
        // Given
        val deepDir = File(tempDir, "a/b/c/d/e/f/g")
        deepDir.mkdirs()
        File(deepDir, "Deep.kt").writeText("class Deep")
        
        // When
        val kotlinFiles = scanner.scanKotlinFiles(tempDir)
        
        // Then
        assertEquals(1, kotlinFiles.size)
        assertEquals("Deep.kt", kotlinFiles.first().name)
    }
    
    @Test
    fun `scanner handles files with multiple dots in name`() {
        // Given
        val srcDir = File(tempDir, "src")
        srcDir.mkdirs()
        File(srcDir, "File.Test.Utils.kt").writeText("object Utils")
        
        // When
        val kotlinFiles = scanner.scanKotlinFiles(tempDir)
        
        // Then
        assertEquals(1, kotlinFiles.size)
        assertEquals("File.Test.Utils.kt", kotlinFiles.first().name)
    }
}
