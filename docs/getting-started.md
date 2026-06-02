# Getting Started

Apply the plugin:

```kotlin
plugins {
    id("com.rollylindenshnizzer.nexuscore.gradle") version "2.0.0"
}
```

Configure shared metadata and targets:

```kotlin
nexuscore {
    modId.set("examplemod")
    group.set("com.example")
    version.set("1.0.0")
    displayName.set("Example Mod")
    authors.add("Roland")
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
```

Put beginner mod code in `common/src/main/java`.

```java
public final class ExampleMod {
    public static final NexusMod MOD = NexusMod.create("examplemod");

    public static void init() {
        MOD.logger().info("Loaded through NexusCore");
    }
}
```

Run `gradle buildAllTargets` to compile the target matrix configured by the plugin.

Use `entrypointClass` for a public class with a static `init()` method. NexusCore generates the Fabric, Quilt, Forge, and NeoForge loader entrypoints and calls that method after the target service provider is installed.

Useful tasks:

```bash
gradle generateNexusMetadata
gradle generateNexusTargetProjects
gradle buildAllTargets
gradle checkAllTargets
gradle runDatagenAllTargets
gradle smokeTestAllTargets
gradle runFabric1201Client
```
