# Release and Distribution

NexusCore publishes two kinds of artifacts:

- Maven artifacts for developers: API, core, modules, adapters, Gradle plugin, sources, and Javadocs.
- A release bundle for GitHub, Modrinth, CurseForge, or manual archival: `build/distributions/nexuscore-<version>.zip`.

## Local Release

```bash
gradle clean build checkAllTargets validateNexusPackages
gradle publishAllTargets
```

Outputs:

```text
build/repo/
build/distributions/nexuscore-2.0.0.zip
```

## Developer Dependency

Until the Gradle plugin is on the Plugin Portal, point plugin resolution at the NexusCore Maven repository:

```kotlin
pluginManagement {
    repositories {
        mavenLocal()
        maven(System.getenv("NEXUSCORE_MAVEN_URL") ?: "https://maven.example.com/nexuscore")
        gradlePluginPortal()
        mavenCentral()
    }
}
```

The bundled templates also honor `NEXUSCORE_MAVEN_URL=<repo url>`, `-Dnexuscore.mavenUrl=<repo url>`, or a `nexuscore.mavenUrl=<repo url>` entry in the template project's `gradle.properties`.

Then use:

```kotlin
plugins {
    id("com.rollylindenshnizzer.nexuscore.gradle") version "2.0.0"
}

nexuscore {
    modId.set("examplemod")
    group.set("com.example")
    version.set("1.0.0")
    entrypointClass.set("com.example.examplemod.ExampleMod")
}
```

For direct module dependencies:

```kotlin
dependencies {
    implementation("com.rollylindenshnizzer.nexuscore:nexuscore-api:2.0.0")
    implementation("com.rollylindenshnizzer.nexuscore:nexuscore-core:2.0.0")
    implementation("com.rollylindenshnizzer.nexuscore:nexuscore-full:2.0.0")
}
```

## GitHub Packages

Set these environment variables in CI:

```text
GITHUB_REPOSITORY=owner/repo
GITHUB_ACTOR=<github user or bot>
GITHUB_TOKEN=<token with package write permission>
```

Run:

```bash
gradle publishAllTargets
```

The build publishes to the local repo and to GitHub Packages when the GitHub variables are present.

## Remote Maven Repository

Set:

```text
NEXUSCORE_MAVEN_URL=https://maven.example.com/releases
NEXUSCORE_MAVEN_USERNAME=<username>
NEXUSCORE_MAVEN_PASSWORD=<password>
```

Run:

```bash
gradle publishAllTargets
```

## GitHub Release

Install and authenticate the `gh` CLI, then set `GH_TOKEN`.

```bash
gradle createGitHubRelease
```

This uploads `build/distributions/nexuscore-2.0.0.zip` to a `v2.0.0` GitHub release.

## Modrinth

Set:

```text
MODRINTH_TOKEN=<token with VERSION_CREATE permission>
MODRINTH_PROJECT_ID=<project id>
```

Optional Gradle properties:

```text
nexuscore.gameVersions=1.20.1,1.21.1,26.1.2
nexuscore.loaders=fabric,forge,neoforge,quilt
nexuscore.changelog=Release notes text
```

Run:

```bash
gradle uploadModrinthRelease
```

## CurseForge

Set:

```text
CURSEFORGE_TOKEN=<upload token>
CURSEFORGE_PROJECT_ID=<project id>
```

CurseForge upload metadata needs numeric game version IDs:

```bash
gradle uploadCurseForgeRelease -Pnexuscore.curseforgeGameVersionIds=1234,5678
```

CI can use `CURSEFORGE_GAME_VERSION_IDS=1234,5678` instead of the Gradle property.

## One Command

With all relevant tokens configured:

```bash
gradle publishReleaseDistribution
```

Tasks skip sources that are missing credentials, so the same command can run locally and in CI.
