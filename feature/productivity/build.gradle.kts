plugins { alias(libs.plugins.android.library); alias(libs.plugins.kotlin.android); alias(libs.plugins.kotlin.compose); alias(libs.plugins.hilt); alias(libs.plugins.ksp) }
android {
    namespace = "com.nexus.productivity"; compileSdk = 35
    defaultConfig { minSdk = 26 }
    compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
    kotlinOptions { jvmTarget = "17" }
    buildFeatures { compose = true }
}
dependencies {
    implementation(project(":core:designsystem")); implementation(project(":core:domain")); implementation(project(":core:common"))
    implementation(platform(libs.compose.bom)); implementation(libs.bundles.compose)
    implementation(libs.hilt.android); ksp(libs.hilt.compiler)
    implementation(libs.vico.compose); implementation(libs.lottie); implementation(libs.konfetti)
}
