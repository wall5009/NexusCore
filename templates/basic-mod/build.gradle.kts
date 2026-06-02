plugins {
    id("com.rollylindenshnizzer.nexuscore.gradle") version "2.0.0"
}

nexuscore {
    modId.set("examplemod")
    group.set("com.example")
    version.set("1.0.0")
    displayName.set("Example Mod")
    description.set("A basic NexusCore v2 mod.")
    authors.add("Your Name")
    license.set("MIT")
    entrypointClass.set("com.example.examplemod.ExampleMod")

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
