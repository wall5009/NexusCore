pluginManagement {
    repositories {
        val fromGradleProperties = file("gradle.properties").takeIf { it.isFile }?.inputStream()?.use { stream ->
            java.util.Properties().apply { load(stream) }.getProperty("nexuscore.mavenUrl")
        }
        val nexusCoreMaven = listOf(gradle.startParameter.projectProperties["nexuscore.mavenUrl"], System.getProperty("nexuscore.mavenUrl"), System.getenv("NEXUSCORE_MAVEN_URL"), fromGradleProperties).firstOrNull { !it.isNullOrBlank() }
        if (!nexusCoreMaven.isNullOrBlank()) {
            maven(url = if (nexusCoreMaven.startsWith("file:")) java.net.URI(nexusCoreMaven.replace(" ", "%20")) else uri(nexusCoreMaven))
        }
        mavenLocal()
        gradlePluginPortal()
        maven("https://maven.fabricmc.net/")
        maven("https://maven.quiltmc.org/repository/release/")
        maven("https://maven.minecraftforge.net/")
        maven("https://maven.neoforged.net/releases")
        mavenCentral()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        val fromGradleProperties = file("gradle.properties").takeIf { it.isFile }?.inputStream()?.use { stream ->
            java.util.Properties().apply { load(stream) }.getProperty("nexuscore.mavenUrl")
        }
        val nexusCoreMaven = listOf(gradle.startParameter.projectProperties["nexuscore.mavenUrl"], System.getProperty("nexuscore.mavenUrl"), System.getenv("NEXUSCORE_MAVEN_URL"), fromGradleProperties).firstOrNull { !it.isNullOrBlank() }
        if (!nexusCoreMaven.isNullOrBlank()) {
            maven(url = if (nexusCoreMaven.startsWith("file:")) java.net.URI(nexusCoreMaven.replace(" ", "%20")) else uri(nexusCoreMaven))
        }
        mavenLocal()
        maven("https://maven.fabricmc.net/")
        maven("https://maven.quiltmc.org/repository/release/")
        maven("https://maven.minecraftforge.net/")
        maven("https://maven.neoforged.net/releases")
        mavenCentral()
    }
}

rootProject.name = "ExampleNexusCoreLibrary"
