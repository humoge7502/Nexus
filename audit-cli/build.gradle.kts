plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
    application
}

dependencies {
    // Project dependencies
    implementation(project(":audit-core"))
    implementation(project(":audit-scanner"))
    implementation(project(":phase-analyzers"))
    implementation(project(":audit-rules"))
    implementation(project(":audit-reporting"))
    
    // Kotlin stdlib
    implementation(libs.kotlin.stdlib)
    
    // Coroutines
    implementation(libs.kotlinx.coroutines.core)
    
    // Serialization
    implementation(libs.kotlinx.serialization)
    
    // Testing
    testImplementation(libs.junit)
    testImplementation(libs.coroutines.test)
}

application {
    mainClass.set("com.nexus.audit.cli.AuditCLIKt")
}

/**
 * Runs the full 8-phase production-readiness audit.
 *
 *   ./gradlew runProductionAudit
 *   ./gradlew runProductionAudit -PauditOut=/tmp/audit
 */
tasks.register<JavaExec>("runProductionAudit") {
    group = "verification"
    description = "Runs the 8-phase production-readiness audit and writes md/json/html reports."
    mainClass.set("com.nexus.audit.cli.AuditCLIKt")
    classpath = sourceSets["main"].runtimeClasspath
    workingDir = rootProject.projectDir
    val out = (project.findProperty("auditOut") as String?)
        ?: "${rootProject.projectDir}/build/audit"
    args("--root", rootProject.projectDir.absolutePath, "--out", out, "--format", "md,json,html")
}

// `--root .` must mean the repository, not this module directory.
tasks.named<JavaExec>("run") {
    workingDir = rootProject.projectDir
}

kotlin {
    jvmToolchain(17)
}
