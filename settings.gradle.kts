pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
    }
}

rootProject.name = "NexusCore"

include(":nexuscore-api")
include(":nexuscore-core")
include(":nexuscore-gradle")

include(":nexuscore-adapters:common")
include(":nexuscore-adapters:mc_1_20_1")
include(":nexuscore-adapters:mc_1_21_1")
include(":nexuscore-adapters:mc_26_1_2")
include(":nexuscore-adapters:fabric")
include(":nexuscore-adapters:forge")
include(":nexuscore-adapters:neoforge")
include(":nexuscore-adapters:quilt")
include(":nexuscore-adapters:fabric_1_20_1")
include(":nexuscore-adapters:forge_1_20_1")
include(":nexuscore-adapters:quilt_1_20_1")
include(":nexuscore-adapters:fabric_1_21_1")
include(":nexuscore-adapters:neoforge_1_21_1")
include(":nexuscore-adapters:quilt_1_21_1")
include(":nexuscore-adapters:fabric_26_1_2")
include(":nexuscore-adapters:neoforge_26_1_2")

include(":nexuscore-modules:registry")
include(":nexuscore-modules:events")
include(":nexuscore-modules:networking")
include(":nexuscore-modules:config")
include(":nexuscore-modules:commands")
include(":nexuscore-modules:datagen")
include(":nexuscore-modules:client")
include(":nexuscore-modules:world")
include(":nexuscore-modules:ui")
include(":nexuscore-modules:advanced")
include(":nexuscore-full")

include(":nexuscore-testmod:common")
include(":nexuscore-testmod:mc_1_20_1")
include(":nexuscore-testmod:mc_1_21_1")
include(":nexuscore-testmod:mc_26_1_2")
include(":nexuscore-testmod:fabric")
include(":nexuscore-testmod:forge")
include(":nexuscore-testmod:neoforge")
include(":nexuscore-testmod:quilt")
include(":nexuscore-testmod:fabric_1_20_1")
include(":nexuscore-testmod:forge_1_20_1")
include(":nexuscore-testmod:quilt_1_20_1")
include(":nexuscore-testmod:fabric_1_21_1")
include(":nexuscore-testmod:neoforge_1_21_1")
include(":nexuscore-testmod:quilt_1_21_1")
include(":nexuscore-testmod:fabric_26_1_2")
include(":nexuscore-testmod:neoforge_26_1_2")
