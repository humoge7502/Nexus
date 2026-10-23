plugins { alias(libs.plugins.android.library); alias(libs.plugins.kotlin.android) }
android {
    namespace = "com.nexus.common"; compileSdk = 35
    defaultConfig { minSdk = 26 }
    compileOptions { sourceCompatibility = JavaVersion.VERSION_17; targetCompatibility = JavaVersion.VERSION_17 }
    kotlinOptions { jvmTarget = "17" }
}
dependencies { implementation(libs.kotlinx.serialization); implementation(libs.kotlinx.collections) 
    // Unit tests
    testImplementation(libs.junit)
    testImplementation(libs.coroutines.test)
}
