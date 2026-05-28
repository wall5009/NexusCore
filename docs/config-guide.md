# Config Guide

`NexusConfig` provides typed options, validation, metadata, schema export, sync diagnostics, presets, migrations, and generated owo config screens.

## Define A Config

```java
public final class ExampleConfig extends NexusConfig {
    public final IntOption machineEnergyCost;
    public final BooleanOption enableParticles;
    public final StringOption workbenchLabel;
    public final EnumOption<Mode> mode;

    public ExampleConfig() {
        super("example");

        machineEnergyCost = intOption("machine_energy_cost", 100).range(1, 10_000);
        machineEnergyCost.group("machine")
                .comment("Energy consumed per operation.")
                .translationKey("config.example.machine_energy_cost")
                .serverSynced()
                .requiresWorldReload();

        enableParticles = booleanOption("enable_particles", true);
        enableParticles.group("client")
                .comment("Enables optional client particles.")
                .translationKey("config.example.enable_particles");

        workbenchLabel = stringOption("workbench_label", "Ruby Workbench").notBlank();
        workbenchLabel.group("ui")
                .translationKey("config.example.workbench_label")
                .serverSynced();

        mode = enumOption("mode", Mode.BALANCED, Mode.class);
        mode.group("machine")
                .translationKey("config.example.mode")
                .serverSynced();
    }
}
```

Option families:

- `IntOption`: optional range metadata and numeric validation.
- `BooleanOption`: on/off flags.
- `StringOption`: regex and non-blank helpers.
- `EnumOption`: enum-backed cycle choices.
- `ConfigOption<T>`: shared comments, groups, translation keys, visibility, enabled state, dependencies, conflicts, restart/world-reload flags, and server-sync flags.

## Validate

Call this during initialization:

```java
config.validateAll();
```

Invalid values throw `InvalidConfigException` with the option key and failed rule. Use validation early so broken config does not partially initialize world state.

## Schema Export

```java
ConfigSchemaExporter.jsonSchema(config, "1.2");
```

The exported schema includes option keys, defaults, types, groups, comments, reload/restart flags, sync metadata, and validation information where possible. This feeds generated docs and editor tooling.

## Presets

Presets describe named value groups:

```java
ConfigPreset starter = ConfigPreset.builder("starter")
        .description("Fast early-game machine settings")
        .icon("example:ruby")
        .value("machine_energy_cost", 80)
        .value("enable_particles", true)
        .build();
```

Presets are data. Apply them through your config import/update path rather than hardcoding gameplay behavior around preset IDs.

## Migrations

Use `ConfigMigration` for config map changes:

```java
ConfigMigration migration = new ConfigMigration(1, 2, values ->
        values.putIfAbsent("workbench_label", "Ruby Workbench"));
migration.apply(values);
```

Use `DataMigrationChain<T>` for saved data and NBT-like payloads.

## Generated owo Config Screen

Every `NexusConfig` registers itself in `NexusConfigRegistry`. On the client:

```java
Screen screen = NexusConfigScreens.create(config, parent);
NexusConfigScreens.open(config, parent);
```

The generated screen renders:

- Booleans as checkboxes.
- Ranged ints as edit box plus slider.
- Unranged ints as edit boxes.
- Strings as edit boxes with validators.
- Enums as cycle buttons.
- Comments, restart/world-reload flags, server-sync flags, reset buttons, reset-all, validation status, and parent navigation.

Common code can use the reflective bridge:

```java
Object screen = OwoConfigBridge.createGeneratedScreen(config, parentScreen);
```

That bridge checks owo availability and avoids importing `net.minecraft.client` classes from dedicated-server paths.

## Dependency Graph And Sync Diagnostics

Use `ConfigDependencyGraph.analyze(config)` when options depend on or conflict with each other. Use `ConfigSyncDiagnostics` to review server-synced options before connecting them to packet sync.

## owo-lib Dependency

The config screen is backed by owo-lib:

- `common/build.gradle` uses owo as compile-only so common APIs compile without bundling client runtime.
- `fabric/build.gradle` uses Fabric owo runtime.
- `neoforge/build.gradle` uses NeoForge owo runtime plus required libraries.
- Loader metadata requires `owo`.

Definitions remain loader-neutral. Screens are client-only.

## Example

The scaffolded coverage config in `a scaffolded NexusConfig` covers int, boolean, string, and enum options. `NexusCoreGameTestScenarios` demonstrates presets, migrations, option metadata, and owo bridge status.
