import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
    alias(libs.plugins.google.services)
    alias(libs.plugins.baselineprofile)
    // Re-enable when a real google-services.json + Crashlytics credentials are
    // available to CI: alias(libs.plugins.firebase.crashlytics)
}

val localProps = Properties().apply {
    val f = rootProject.file("local.properties")
    if (f.exists()) f.inputStream().use { load(it) }
}

android {
    namespace = "com.nexus.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.nexus.app"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField(
            "String",
            "GOOGLE_WEB_CLIENT_ID",
            "\"${localProps.getProperty("GOOGLE_WEB_CLIENT_ID") ?: ""}\""
        )
    }

    signingConfigs {
        create("release") {
            val keystorePath = localProps.getProperty("KEYSTORE_PATH")?.ifBlank { null }
            if (keystorePath != null) {
                storeFile = file(keystorePath)
                storePassword = localProps.getProperty("STORE_PASSWORD")
                keyAlias = localProps.getProperty("KEY_ALIAS")
                keyPassword = localProps.getProperty("KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // An empty KEYSTORE_PATH (CI without signing secrets) must produce an
            // unsigned APK, not a signing attempt against a non-existent file.
            val hasKeystore = localProps.getProperty("KEYSTORE_PATH")?.ifBlank { null } != null
            if (hasKeystore) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
        // Profile-generation build: R8 off so the collected profile maps to
        // real symbols, profileable injected by the baselineprofile plugin.
        create("benchmark") {
            initWith(getByName("release"))
            matchingFallbacks += listOf("release")
            isDebuggable = false
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"))
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    lint {
        lintConfig = file("lint.xml")
        baseline = file("lint-baseline.xml")
        abortOnError = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // Core modules
    implementation(project(":core:common"))
    implementation(project(":core:designsystem"))
    implementation(project(":core:domain"))
    implementation(project(":core:data"))
    implementation(project(":core:network"))
    implementation(project(":core:datastore"))
    implementation(project(":core:notifications"))
    implementation(project(":core:sync"))

    // Background work / Hilt worker factory
    implementation(libs.work)
    implementation(libs.hilt.work)

    // Feature modules wired into Navigation
    implementation(project(":feature:onboarding"))
    implementation(project(":feature:home"))
    implementation(project(":feature:internships"))
    implementation(project(":feature:certifications"))
    implementation(project(":feature:productivity"))
    implementation(project(":feature:profile"))
    implementation(project(":feature:achievements"))
    implementation(project(":feature:settings"))
    implementation(project(":feature:auth"))
    implementation(project(":feature:explore"))
    implementation(project(":feature:ai"))
    implementation(project(":feature:growth"))
    implementation(project(":feature:goals"))
    implementation(project(":feature:notifications"))
    implementation(project(":feature:paywall"))
    implementation(project(":feature:applications"))
    implementation(project(":feature:careermirror"))
    implementation(project(":feature:cohortintelligence"))
    implementation(project(":feature:community"))
    implementation(project(":feature:interviewlog"))
    implementation(project(":feature:placementforecast"))
    implementation(project(":feature:portfolio"))
    implementation(project(":feature:search"))
    implementation(project(":feature:tribe"))

    // Compose
    implementation(platform(libs.compose.bom))
    implementation(libs.bundles.compose)
    implementation(libs.compose.ui.tooling.preview)
    debugImplementation(libs.compose.ui.tooling)
    implementation(libs.activity.compose)
    implementation(libs.navigation.compose)
    implementation(libs.lifecycle.viewmodel.compose)
    implementation(libs.lifecycle.runtime.compose)

    // DI
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)

    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.functions)
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.messaging)
    implementation("com.google.firebase:firebase-storage:21.0.0")

    // Misc
    implementation(libs.splash)
    implementation(libs.androidx.profileinstaller)
    implementation(libs.datastore)
    implementation(libs.kotlinx.serialization)
    implementation(libs.kotlinx.coroutines.play.services)
    implementation(libs.timber)

    // Consumes the profile produced by :macrobenchmark (androidx.baselineprofile).
    baselineProfile(project(":macrobenchmark"))

    // Tests
    testImplementation(libs.junit)
    testImplementation(libs.coroutines.test)
}

