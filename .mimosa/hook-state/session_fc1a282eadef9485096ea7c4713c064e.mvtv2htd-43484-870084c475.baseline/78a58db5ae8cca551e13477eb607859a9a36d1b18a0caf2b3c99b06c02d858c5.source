plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
}

dependencies {
    // Project dependencies
    implementation(project(":audit-core"))
    
    // Kotlin stdlib
    implementation(libs.kotlin.stdlib)
    
    // Coroutines
    implementation(libs.kotlinx.coroutines.core)
    
    // Serialization
    implementation(libs.kotlinx.serialization)
    
    // Testing
    testImplementation(libs.junit)
    testImplementation(libs.kotlin.test)
    testImplementation(libs.coroutines.test)
}

kotlin {
    jvmToolchain(17)
}
