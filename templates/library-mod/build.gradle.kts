plugins {
    id("com.rollylindenshnizzer.nexuscore.gradle") version "2.0.0"
    `maven-publish`
}

nexuscore {
    modId.set("examplelibrary")
    group.set("com.example")
    version.set("1.0.0")
    displayName.set("Example Library")
    description.set("A NexusCore-based API library for other mods.")
    authors.add("Your Name")
    license.set("MIT")
    entrypointClass.set("com.example.examplelibrary.ExampleLibrary")

    targets {
        minecraft("1.21.1") {
            loaders("neoforge", "fabric", "quilt")
        }
    }
}

publishing {
    repositories {
        maven {
            name = "localRelease"
            url = layout.buildDirectory.dir("repo").get().asFile.toURI()
        }
    }
}
