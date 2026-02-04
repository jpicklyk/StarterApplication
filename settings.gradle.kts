pluginManagement {
    includeBuild("build-logic")
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

@Suppress("UnstableApiUsage")
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

rootProject.name = "StarterApplication"
include(":app")
include(":core:designsystem")
// Removed empty modules: :core:common, :core:testing, :feature1

// Knox Core submodules
include(":knox-core")
include(":knox-core:android")
include(":knox-core:common")
include(":knox-core:feature")
include(":knox-core:feature-processor")
include(":knox-core:testing")
include(":knox-core:ui")
include(":knox-core:usecase-executor")

// Knox modules
include(":knox-enterprise")
include(":knox-licensing")
include(":knox-hilt")
