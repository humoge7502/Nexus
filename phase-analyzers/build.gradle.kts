plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
}

dependencies {
    // Project dependencies
    implementation(project(":audit-core"))
    implementation(project(":audit-scanner"))
    implementation(project(":audit-rules"))
    
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

kotlin {
    jvmToolchain(17)
}
