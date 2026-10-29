plugins { alias(libs.plugins.android.library); alias(libs.plugins.kotlin.android); alias(libs.plugins.kotlin.compose); alias(libs.plugins.hilt); alias(libs.plugins.ksp) }
android {
    namespace = "com.nexus.achievements"; compileSdk = 36
    defaultConfig { minSdk = 26 }
    compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
    kotlinOptions { jvmTarget = "17" }
    buildFeatures { compose = true }
}
dependencies {
    implementation(project(":core:designsystem"))
    implementation(libs.hilt.navigation.compose)
    implementation(libs.timber)
    implementation(project(":core:analytics"))
    implementation(project(":core:common"))
    implementation(project(":core:data"))
    implementation(project(":core:domain"))
    implementation(platform(libs.compose.bom)); implementation(libs.bundles.compose)
    implementation(libs.hilt.android); ksp(libs.hilt.compiler); implementation(libs.lottie); implementation(libs.konfetti)
    implementation(libs.kotlinx.collections)

    implementation(libs.lifecycle.runtime.compose)
    testImplementation(libs.junit)
}
