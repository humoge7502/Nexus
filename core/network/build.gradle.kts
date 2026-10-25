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
        debug {
            buildConfigField(
                "String",
                "OPENROUTER_API_KEY",
                "\"${localProps.getProperty("OPENROUTER_API_KEY") ?: ""}\""
            )
        }
        release {
            // Never read from local.properties here. The upstream key belongs
            // to the proxyAI Cloud Function; compiling it into a distributable
            // artifact would expose it in the APK regardless of runtime routing.
            buildConfigField("String", "OPENROUTER_API_KEY", "\"\"")
        }
        all {
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
    implementation(libs.timber)
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

// Windows file-lock workaround: keep test outputs out of the watched
// build/test-results path, which an external process keeps holding open.
tasks.withType<Test>().configureEach {
    binaryResultsDirectory.set(layout.buildDirectory.dir("test-binary-results/testDebugUnitTest"))
    reports.html.outputLocation.set(layout.buildDirectory.dir("test-reports/testDebugUnitTest"))
    reports.junitXml.outputLocation.set(layout.buildDirectory.dir("test-xml-results/testDebugUnitTest"))
}
