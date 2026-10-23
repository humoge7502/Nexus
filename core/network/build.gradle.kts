import java.util.Properties

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

val localProps = Properties().apply {
    val f = rootProject.file("local.properties")
    if (f.exists()) f.inputStream().use { load(it) }
}

android {
    namespace = "com.nexus.network"
    compileSdk = 35
    defaultConfig { minSdk = 26 }
    buildFeatures {
        buildConfig = true
    }
    buildTypes {
        all {
            // Debug builds may call OpenRouter directly with a local key;
            // release builds always go through the Cloud Function proxy.
            buildConfigField(
                "String",
                "OPENROUTER_API_KEY",
                "\"${localProps.getProperty("OPENROUTER_API_KEY") ?: ""}\""
            )
            buildConfigField(
                "String",
                "AI_PROXY_URL",
                "\"${localProps.getProperty("AI_PROXY_URL") ?: ""}\""
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:datastore"))
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.bundles.retrofit)
    implementation(libs.kotlinx.serialization)
    implementation(libs.kotlinx.coroutines.play.services)

    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.functions)

    // Unit tests
    testImplementation(libs.junit)
    testImplementation(libs.coroutines.test)
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.kotlin)
    testImplementation(libs.mockwebserver)
    testImplementation(platform(libs.firebase.bom))
    testImplementation(libs.firebase.auth)
}
