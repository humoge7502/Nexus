plugins { alias(libs.plugins.android.library); alias(libs.plugins.kotlin.android); alias(libs.plugins.kotlin.compose); alias(libs.plugins.hilt); alias(libs.plugins.ksp) }
android {
    namespace = "com.nexus.search"; compileSdk = 36
    defaultConfig { minSdk = 26 }
    compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
    kotlinOptions { jvmTarget = "17" }
    buildFeatures { compose = true }
}
dependencies {
    implementation(project(":core:designsystem"))
    implementation(project(":core:domain"))
    implementation(project(":core:data"))
    implementation(project(":core:datastore"))
    implementation(project(":core:common"))
    implementation(project(":core:analytics"))
    implementation(platform(libs.compose.bom)); implementation(libs.bundles.compose)
    implementation(libs.hilt.android); ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.paging.runtime); implementation(libs.paging.compose); implementation(libs.datastore)

    implementation(libs.lifecycle.runtime.compose)
}
