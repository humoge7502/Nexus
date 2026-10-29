plugins {
alias(libs.plugins.android.library)
alias(libs.plugins.kotlin.android)
alias(libs.plugins.kotlin.compose)
alias(libs.plugins.hilt)
alias(libs.plugins.ksp)
}

android {
namespace = "com.nexus.tribe"
compileSdk = 36

defaultConfig {
minSdk = 26
testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
}

compileOptions {
sourceCompatibility = JavaVersion.VERSION_17
targetCompatibility = JavaVersion.VERSION_17
}

kotlinOptions {
jvmTarget = "17"
}

buildFeatures {
compose = true
}
}

dependencies {
    implementation(project(":core:designsystem"))
    implementation(project(":core:datastore"))
    implementation(project(":core:domain"))
    implementation(project(":core:data"))
    implementation(project(":core:analytics"))
    implementation(platform(libs.compose.bom))
    implementation(libs.bundles.compose)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.navigation.compose)
    implementation(libs.lifecycle.viewmodel.compose)
    implementation(libs.lifecycle.runtime.compose)

    // Unit tests
    testImplementation(libs.junit)
    testImplementation(libs.coroutines.test)
    testImplementation(libs.mockito.core)
    testImplementation(libs.mockito.kotlin)

    // Compose UI tests
    androidTestImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.compose.ui.test.junit4)
    debugImplementation(libs.compose.ui.test.manifest)
}
