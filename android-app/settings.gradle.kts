pluginManagement {
    includeBuild("build-logic")
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "ProductCatalogue"

include(":app")

include(":core:model")
include(":core:network")
include(":core:designsystem")
include(":core:ui")
include(":core:testing")

include(":feature:catalogue")
include(":feature:enquiry")
