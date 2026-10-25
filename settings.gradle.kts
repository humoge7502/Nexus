pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.10.0"
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Nexus"

include(":app")

// Core modules
include(":core:designsystem")
include(":core:domain")
include(":core:data")
include(":core:network")
include(":core:database")
include(":core:datastore")
include(":core:common")
include(":core:ui")
include(":core:analytics")
include(":core:notifications")
include(":core:sync")

// Feature modules
include(":feature:onboarding")
include(":feature:home")
include(":feature:internships")
include(":feature:certifications")
include(":feature:productivity")
include(":feature:profile")
include(":feature:achievements")
include(":feature:settings")
include(":feature:auth")
include(":feature:explore")
include(":feature:ai")
include(":feature:growth")
include(":feature:goals")
include(":feature:notifications")
include(":feature:applications")
include(":feature:careermirror")
include(":feature:cohortintelligence")
include(":feature:community")
include(":feature:interviewlog")
include(":feature:paywall")
include(":feature:placementforecast")
include(":feature:portfolio")
include(":feature:search")
include(":feature:tribe")

// Internal audit tooling (pure JVM)
include(":audit-core")
include(":audit-scanner")
include(":audit-rules")
include(":audit-reporting")
include(":audit-cli")
include(":phase-analyzers")

// Baseline-profile generator (com.android.test). Requires an emulator/device
// to collect a profile; compile-only on CI is still useful.
include(":macrobenchmark")
