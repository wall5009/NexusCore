plugins {
    id("com.rollylindenshnizzer.nexuscore.gradle") version "2.0.0"
}

nexuscore {
    modId.set("advancedexample")
    group.set("com.example")
    version.set("1.0.0")
    displayName.set("Advanced Example")
    description.set("A NexusCore mod with common, version, loader, and target source folders.")
    authors.add("Your Name")
    license.set("MIT")
    entrypointClass.set("com.example.advancedexample.AdvancedExampleMod")

    targets {
        minecraft("1.20.1") {
            loaders("forge", "fabric", "quilt")
        }
        minecraft("1.21.1") {
            loaders("neoforge", "fabric", "quilt")
        }
        minecraft("26.1.2") {
            loaders("neoforge", "fabric")
        }
    }
}
