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

kotlin {
    jvmToolchain(17)
}
