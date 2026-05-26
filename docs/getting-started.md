# Getting Started

NexusCore is an Architectury library for Minecraft 1.21.1. The public API lives in `common`, while `fabric` and `neoforge` provide loader entrypoints, metadata, optional compatibility plugins, datagen providers, GameTest registration, and transfer/capability bridges.

## Add NexusCore To A Mod

Depend on the loader jar at runtime and compile against the common API. In a multi-loader Architectury mod, your shared module should use NexusCore common classes and each loader module should depend on the matching NexusCore loader jar.

The required runtime dependencies are:

- Architectury API.
- owo-lib.
- Fabric API on Fabric.
- NeoForge on NeoForge.

Optional integrations are declared as optional metadata dependencies and compile-only Gradle dependencies in NexusCore itself:

- JEI.
- EMI.
- REI.
- Team Reborn Energy API on Fabric.

## Bootstrap A Mod

Use `NexusMod` when you want a predictable initialization flow:

```java
public final class ExampleContent extends NexusMod {
    public static final String MOD_ID = "example";

    public ExampleContent() {
        super(MOD_ID);
    }

    @Override
    protected void beforeRegistries() {
        NexusItems.item(MOD_ID, "ruby")
                .modelGenerated()
                .register();
    }

    @Override
    protected void onInitialize() {
        logger().info("Example initialized");
    }
}
```

Call it from each loader:

```java
NexusCore.init();
new ExampleContent().init();
```

`beforeRegistries()` is for content declarations. `onInitialize()` is for commands, networking, config validation, debug sections, and compatibility descriptors.

## Use The Example Mod

The example is a full dual-loader sample:

- `examples/example-mod/common` contains shared content and datagen declarations.
- `examples/example-mod/fabric` contains the Fabric entrypoint and Fabric datagen entrypoint.
- `examples/example-mod/neoforge` contains the NeoForge `@Mod` entrypoint and NeoForge datagen listener.

Build everything:

```powershell
.\gradlew.bat build
```

Run datagen:

```powershell
.\gradlew.bat :fabric:runDatagen
.\gradlew.bat :neoforge:runDatagen
.\gradlew.bat :example-fabric:runDatagen
.\gradlew.bat :example-neoforge:runDatagen
```

Run GameTests:

```powershell
.\gradlew.bat :fabric:runGametest
.\gradlew.bat :neoforge:runGametest
```
