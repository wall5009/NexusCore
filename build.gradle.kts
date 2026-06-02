import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.jvm.tasks.Jar
import org.gradle.plugin.devel.GradlePluginDevelopmentExtension
import org.gradle.api.tasks.bundling.Zip

plugins {
    base
}

group = "com.rollylindenshnizzer.nexuscore"
version = "2.0.0"

val javaVersion = JavaVersion.VERSION_17

subprojects {
    group = when {
        path.startsWith(":nexuscore-adapters:") -> "${rootProject.group}.adapters"
        path.startsWith(":nexuscore-testmod:") -> "${rootProject.group}.testmod"
        path.startsWith(":nexuscore-modules:") -> "${rootProject.group}.modules"
        else -> rootProject.group
    }
    version = rootProject.version

    if (path != ":nexuscore-gradle") {
        apply(plugin = "java-library")
    }

    plugins.withId("java") {
        extensions.configure<JavaPluginExtension> {
            sourceCompatibility = javaVersion
            targetCompatibility = javaVersion
            withSourcesJar()
            withJavadocJar()
        }

        tasks.withType<JavaCompile>().configureEach {
            options.encoding = "UTF-8"
            options.release.set(17)
        }

        tasks.withType<Jar>().configureEach {
            manifest {
                attributes(
                    "Implementation-Title" to project.name,
                    "Implementation-Version" to project.version,
                    "Implementation-Vendor" to "NexusCore"
                )
            }
        }

        apply(plugin = "maven-publish")

        extensions.configure<PublishingExtension> {
            publications {
                if (project.path != ":nexuscore-gradle") {
                    create<MavenPublication>("mavenJava") {
                        from(components["java"])
                        pom {
                            name.set(project.name)
                            description.set("NexusCore v2 ${project.name} artifact")
                            url.set("https://github.com/rollylindenshnizzer/NexusCore")
                            licenses {
                                license {
                                    name.set("MIT")
                                    url.set("https://opensource.org/license/mit")
                                }
                            }
                        }
                    }
                }
            }
            repositories {
                maven {
                    name = "nexuscoreLocal"
                    url = rootProject.layout.buildDirectory.dir("repo").get().asFile.toURI()
                }
                val remoteUrl = rootProject.providers.gradleProperty("nexuscore.mavenUrl")
                    .orElse(rootProject.providers.environmentVariable("NEXUSCORE_MAVEN_URL"))
                    .orNull
                if (!remoteUrl.isNullOrBlank()) {
                    maven {
                        name = "nexuscoreRemote"
                        url = uri(remoteUrl)
                        credentials {
                            username = rootProject.providers.gradleProperty("nexuscore.mavenUsername")
                                .orElse(rootProject.providers.environmentVariable("NEXUSCORE_MAVEN_USERNAME"))
                                .orNull
                            password = rootProject.providers.gradleProperty("nexuscore.mavenPassword")
                                .orElse(rootProject.providers.environmentVariable("NEXUSCORE_MAVEN_PASSWORD"))
                                .orNull
                        }
                    }
                }
                val githubRepository = rootProject.providers.gradleProperty("nexuscore.githubRepository")
                    .orElse(rootProject.providers.environmentVariable("GITHUB_REPOSITORY"))
                    .orNull
                if (!githubRepository.isNullOrBlank()) {
                    maven {
                        name = "githubPackages"
                        url = uri("https://maven.pkg.github.com/$githubRepository")
                        credentials {
                            username = rootProject.providers.gradleProperty("nexuscore.githubActor")
                                .orElse(rootProject.providers.environmentVariable("GITHUB_ACTOR"))
                                .orNull
                            password = rootProject.providers.gradleProperty("nexuscore.githubToken")
                                .orElse(rootProject.providers.environmentVariable("GITHUB_TOKEN"))
                                .orNull
                        }
                    }
                }
            }
        }
    }
}

project(":nexuscore-core") {
    dependencies {
        "api"(project(":nexuscore-api"))
    }
}

project(":nexuscore-gradle") {
    apply(plugin = "java-gradle-plugin")

    dependencies {
        "implementation"(gradleApi())
    }

    extensions.configure<GradlePluginDevelopmentExtension> {
        plugins {
            create("nexuscore") {
                id = "com.rollylindenshnizzer.nexuscore.gradle"
                implementationClass = "com.rollylindenshnizzer.nexuscore.gradle.NexusCoreGradlePlugin"
                displayName = "NexusCore Gradle Plugin"
                description = "Generates NexusCore multi-loader, multi-version Minecraft mod targets."
            }
        }
    }
}

subprojects
    .filter { it.path.startsWith(":nexuscore-modules:") || it.path == ":nexuscore-full" }
    .forEach { module ->
        module.dependencies {
            "api"(project(":nexuscore-api"))
            "implementation"(project(":nexuscore-core"))
        }
    }

subprojects
    .filter { it.path.startsWith(":nexuscore-adapters:") }
    .forEach { adapter ->
        adapter.dependencies {
            "api"(project(":nexuscore-api"))
            "api"(project(":nexuscore-core"))
            if (adapter.path != ":nexuscore-adapters:common") {
                "api"(project(":nexuscore-adapters:common"))
            }
        }
    }

subprojects
    .filter { it.path.startsWith(":nexuscore-testmod:") }
    .forEach { testmod ->
        testmod.dependencies {
            "implementation"(project(":nexuscore-api"))
            "implementation"(project(":nexuscore-core"))
            if (testmod.path != ":nexuscore-testmod:common") {
                "implementation"(project(":nexuscore-testmod:common"))
            }
        }
    }

mapOf(
    ":nexuscore-testmod:fabric_1_20_1" to ":nexuscore-adapters:fabric_1_20_1",
    ":nexuscore-testmod:forge_1_20_1" to ":nexuscore-adapters:forge_1_20_1",
    ":nexuscore-testmod:quilt_1_20_1" to ":nexuscore-adapters:quilt_1_20_1",
    ":nexuscore-testmod:fabric_1_21_1" to ":nexuscore-adapters:fabric_1_21_1",
    ":nexuscore-testmod:neoforge_1_21_1" to ":nexuscore-adapters:neoforge_1_21_1",
    ":nexuscore-testmod:quilt_1_21_1" to ":nexuscore-adapters:quilt_1_21_1",
    ":nexuscore-testmod:fabric_26_1_2" to ":nexuscore-adapters:fabric_26_1_2",
    ":nexuscore-testmod:neoforge_26_1_2" to ":nexuscore-adapters:neoforge_26_1_2"
).forEach { (testmodPath, adapterPath) ->
    project(testmodPath).dependencies {
        "implementation"(project(adapterPath))
    }
}

tasks.register("buildAllTargets") {
    group = "nexuscore"
    description = "Builds every NexusCore adapter and test-mod target project."
    dependsOn(subprojects.filter { it.path.startsWith(":nexuscore-adapters:") || it.path.startsWith(":nexuscore-testmod:") }.map { "${it.path}:build" })
}

tasks.register("checkAllTargets") {
    group = "nexuscore"
    description = "Runs checks for every NexusCore adapter and test-mod target project."
    dependsOn(subprojects.filter { it.path.startsWith(":nexuscore-adapters:") || it.path.startsWith(":nexuscore-testmod:") }.map { "${it.path}:check" })
}

tasks.register("runDatagenAllTargets") {
    group = "nexuscore"
    description = "Runs all generated datagen tasks. In this source tree the API datagen implementation writes deterministic JSON resources."
}

tasks.register("validateNexusPackages") {
    group = "verification"
    description = "Validates that generated NexusCore jars are present and non-empty."
    val jarTasks = subprojects.mapNotNull { it.tasks.findByName("jar") }
    dependsOn(jarTasks)
    doLast {
        subprojects
            .filter { it.plugins.hasPlugin("java") }
            .forEach { project ->
                val libs = project.layout.buildDirectory.dir("libs").get().asFile
                val jars = libs.listFiles { file -> file.extension == "jar" && !file.name.endsWith("-sources.jar") && !file.name.endsWith("-javadoc.jar") }.orEmpty()
                require(jars.isNotEmpty()) { "NexusCore package validation failed: ${project.path} did not produce a runtime jar." }
                jars.forEach { jar ->
                    require(jar.length() > 0L) { "NexusCore package validation failed: ${jar.absolutePath} is empty." }
                }
            }
    }
}

tasks.register<Zip>("releaseBundle") {
    group = "publishing"
    description = "Packages NexusCore jars, docs, templates, and generated metadata into one release archive."
    archiveBaseName.set("nexuscore")
    archiveVersion.set(project.version.toString())
    from(layout.buildDirectory.dir("repo")) {
        into("maven-repo")
    }
    from("docs") {
        into("docs")
    }
    from("templates") {
        into("templates")
        exclude("**/build/**", "**/.gradle/**")
    }
    subprojects.forEach { project ->
        from(project.layout.buildDirectory.dir("libs")) {
            into("libs/${project.path.removePrefix(":").replace(":", "/")}")
        }
    }
    dependsOn("buildAllTargets", "validateNexusPackages")
    dependsOn(subprojects.flatMap { project ->
        listOfNotNull(
            project.tasks.findByName("jar"),
            project.tasks.findByName("sourcesJar"),
            project.tasks.findByName("javadocJar")
        )
    })
}

tasks.register("publishAllTargets") {
    group = "nexuscore"
    description = "Publishes every NexusCore artifact to the local NexusCore release repository and assembles the release bundle."
    dependsOn(subprojects.flatMap { project ->
        listOf(
            "publishAllPublicationsToNexuscoreLocalRepository",
            "publishAllPublicationsToNexuscoreRemoteRepository",
            "publishAllPublicationsToGithubPackagesRepository"
        ).mapNotNull { project.tasks.findByName(it) }
    })
    dependsOn("releaseBundle")
}

tasks.register("createGitHubRelease") {
    group = "publishing"
    description = "Creates a GitHub release with gh CLI when GH_TOKEN and repository access are configured."
    dependsOn("releaseBundle")
    doLast {
        val token = providers.environmentVariable("GH_TOKEN").orNull
        if (token.isNullOrBlank()) {
            logger.lifecycle("GH_TOKEN is not set; release bundle was created but GitHub release upload was skipped.")
            return@doLast
        }
        providers.exec {
            commandLine(
                "gh",
                "release",
                "create",
                "v${project.version}",
                layout.buildDirectory.file("distributions/nexuscore-${project.version}.zip").get().asFile.absolutePath,
                "--title",
                "NexusCore v${project.version}",
                "--notes",
                "NexusCore v2 release bundle."
            )
        }.result.get().assertNormalExitValue()
    }
}

tasks.register("uploadModrinthRelease") {
    group = "publishing"
    description = "Uploads the NexusCore release bundle to Modrinth when MODRINTH_TOKEN and project metadata are configured."
    dependsOn("releaseBundle")
    doLast {
        val token = providers.environmentVariable("MODRINTH_TOKEN").orNull
        val projectId = providers.gradleProperty("nexuscore.modrinthProjectId")
            .orElse(providers.environmentVariable("MODRINTH_PROJECT_ID"))
            .orNull
        if (token.isNullOrBlank() || projectId.isNullOrBlank()) {
            logger.lifecycle("MODRINTH_TOKEN or nexuscore.modrinthProjectId/MODRINTH_PROJECT_ID is not set; Modrinth upload skipped.")
            return@doLast
        }
        val file = providers.gradleProperty("nexuscore.releaseFile")
            .map { layout.projectDirectory.file(it).asFile }
            .orElse(layout.buildDirectory.file("distributions/nexuscore-${project.version}.zip").map { it.asFile })
            .get()
        val gameVersions = providers.gradleProperty("nexuscore.gameVersions")
            .orElse("1.20.1,1.21.1,26.1.2")
            .get()
            .split(",")
            .map { "\"${it.trim()}\"" }
            .joinToString(",")
        val loaders = providers.gradleProperty("nexuscore.loaders")
            .orElse("fabric,forge,neoforge,quilt")
            .get()
            .split(",")
            .map { "\"${it.trim()}\"" }
            .joinToString(",")
        val changelog = providers.gradleProperty("nexuscore.changelog")
            .orElse("NexusCore v${project.version} release.")
            .get()
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
        val metadata = """
            {"project_id":"$projectId","name":"NexusCore v${project.version}","version_number":"${project.version}","changelog":"$changelog","dependencies":[],"game_versions":[$gameVersions],"version_type":"release","loaders":[$loaders],"featured":false,"file_parts":["file"],"primary_file":"file"}
            """.trimIndent()
        providers.exec {
            commandLine(
                "curl",
                "--fail",
                "--retry", "3",
                "-X", "POST",
                "https://api.modrinth.com/v2/version",
                "-H", "User-Agent: NexusCore/${project.version} (https://github.com/rollylindenshnizzer/NexusCore)",
                "-H", "Authorization: $token",
                "-F", "data=$metadata",
                "-F", "file=@${file.absolutePath}"
            )
        }.result.get().assertNormalExitValue()
    }
}

tasks.register("uploadCurseForgeRelease") {
    group = "publishing"
    description = "Uploads the NexusCore release bundle to CurseForge when CURSEFORGE_TOKEN and project metadata are configured."
    dependsOn("releaseBundle")
    doLast {
        val token = providers.environmentVariable("CURSEFORGE_TOKEN").orNull
        val projectId = providers.gradleProperty("nexuscore.curseforgeProjectId")
            .orElse(providers.environmentVariable("CURSEFORGE_PROJECT_ID"))
            .orNull
        if (token.isNullOrBlank() || projectId.isNullOrBlank()) {
            logger.lifecycle("CURSEFORGE_TOKEN or nexuscore.curseforgeProjectId/CURSEFORGE_PROJECT_ID is not set; CurseForge upload skipped.")
            return@doLast
        }
        val file = providers.gradleProperty("nexuscore.releaseFile")
            .map { layout.projectDirectory.file(it).asFile }
            .orElse(layout.buildDirectory.file("distributions/nexuscore-${project.version}.zip").map { it.asFile })
            .get()
        val gameVersionIdsValue = providers.gradleProperty("nexuscore.curseforgeGameVersionIds")
            .orElse(providers.environmentVariable("CURSEFORGE_GAME_VERSION_IDS"))
            .orNull
        if (gameVersionIdsValue.isNullOrBlank()) {
            logger.lifecycle("nexuscore.curseforgeGameVersionIds/CURSEFORGE_GAME_VERSION_IDS is not set; CurseForge upload skipped.")
            return@doLast
        }
        val gameVersionIds = gameVersionIdsValue
            .split(",")
            .mapNotNull { it.trim().takeIf(String::isNotBlank) }
            .joinToString(",")
        val changelog = providers.gradleProperty("nexuscore.changelog")
            .orElse("NexusCore v${project.version} release.")
            .get()
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
        val metadata = """
            {"changelog":"$changelog","changelogType":"text","displayName":"NexusCore v${project.version}","releaseType":"release","gameVersions":[$gameVersionIds]}
            """.trimIndent()
        providers.exec {
            commandLine(
                "curl",
                "--fail",
                "--retry", "3",
                "-X", "POST",
                "https://minecraft.curseforge.com/api/projects/$projectId/upload-file",
                "-H", "User-Agent: NexusCore/${project.version} (https://github.com/rollylindenshnizzer/NexusCore)",
                "-H", "X-Api-Token: $token",
                "-F", "metadata=$metadata",
                "-F", "file=@${file.absolutePath}"
            )
        }.result.get().assertNormalExitValue()
    }
}

tasks.register("publishReleaseDistribution") {
    group = "publishing"
    description = "Builds the release bundle and runs every configured distribution upload task."
    dependsOn("publishAllTargets", "createGitHubRelease", "uploadModrinthRelease", "uploadCurseForgeRelease")
}
