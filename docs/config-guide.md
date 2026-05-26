# Config Guide

`NexusConfig` gives mods typed options with validation metadata, presets, migrations, and import/export support.

## Define Options

```java
public final class ExampleConfig extends NexusConfig {
    public final IntOption machineEnergyCost;

    public ExampleConfig() {
        super("example");
        machineEnergyCost = intOption("machine_energy_cost", 100)
                .range(1, 10_000)
                .serverSynced()
                .requiresWorldReload();
    }
}
```

Available option types include boolean, int, string, and enum options. Each option can carry scope, sync, reload, and validation metadata.

## Validate

Call `validateAll()` during common initialization. Invalid values throw `InvalidConfigException` with a useful message.

## Presets And Migrations

Use `ConfigPreset` for named sets of values and `ConfigMigration` when a config schema changes. `DataMigrationChain` is available for broader saved-data migrations.

## Serialization

`ConfigSerializer` handles import/export. Keep config IO at loader-safe boundaries, then pass validated values into common systems.

## Generated owo Config Screen

Every `NexusConfig` registers itself in `NexusConfigRegistry` when constructed. On the client, create or open a generated owo screen with:

```java
Screen screen = NexusConfigScreens.create(config, parent);
NexusConfigScreens.open(config, parent);
```

The generated editor is built from the typed option metadata:

- Booleans render as checkboxes.
- Ranged ints render as an edit box plus a snapped slider; unranged ints render as an edit box.
- Strings render as edit boxes and keep the option's validators active.
- Enums render as cycle buttons.
- Comments, restart/world-reload/server-sync flags, reset buttons, reset-all, validation status, and parent-screen navigation are generated automatically.

The bridge remains server-safe. If common code needs to create a screen without importing client classes, call:

```java
Object screen = OwoConfigBridge.createGeneratedScreen(config, parentScreen);
```

That method checks owo availability and uses reflection so dedicated servers do not load `net.minecraft.client` classes.

## owo-lib Dependency

The config module does properly depend on owo-lib:

- `common/build.gradle` has `modCompileOnly "io.wispforest:owo-lib:0.12.15.1+1.21"`.
- `fabric/build.gradle` has `modImplementation "io.wispforest:owo-lib:0.12.15.1+1.21"`.
- `neoforge/build.gradle` has `modImplementation "io.wispforest:owo-lib-neoforge:0.12.15-beta.9+1.21"` plus required runtime libraries.
- Fabric metadata requires `owo`; NeoForge metadata requires `owo`.

`OwoConfigBridge` provides availability checks and a reflective generated-screen factory. Config definitions stay loader-neutral, while `NexusConfigScreen` and `NexusConfigScreens` provide the client-only owo editor.
