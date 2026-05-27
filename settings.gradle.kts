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
