plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.nexus.data"
    compileSdk = 36
    defaultConfig { minSdk = 26 }
    buildTypes {
        debug {
            enableUnitTestCoverage = true
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
    implementation(project(":core:domain"))
    implementation(project(":core:network"))
    implementation(project(":core:database"))
    implementation(project(":core:datastore"))
    implementation(project(":core:analytics"))
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.bundles.retrofit)
    implementation(libs.bundles.room)
    ksp(libs.room.compiler)
    implementation(libs.datastore)
    implementation(libs.kotlinx.serialization)
    implementation(libs.kotlinx.collections)
    implementation(libs.billing)
    implementation(libs.kotlinx.coroutines.play.services)
    implementation(libs.timber)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.auth)

    // Unit tests
    testImplementation(libs.junit)
    testImplementation(libs.coroutines.test)

    testImplementation(libs.mockk)
}
