plugins { alias(libs.plugins.android.library); alias(libs.plugins.kotlin.android); alias(libs.plugins.hilt); alias(libs.plugins.ksp) }
android {
    namespace = "com.nexus.analytics"; compileSdk = 36
    defaultConfig { minSdk = 26 }
    compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
    kotlinOptions { jvmTarget = "17" }
}
dependencies {
    implementation(libs.hilt.android); ksp(libs.hilt.compiler)
    implementation(platform(libs.firebase.bom))
    implementation("com.google.firebase:firebase-config")
    implementation(libs.kotlinx.coroutines.play.services)
    implementation(libs.timber)

    // Unit tests
    testImplementation(libs.junit)
    testImplementation(libs.coroutines.test)

    testImplementation(libs.mockk)
}
