# Example Mod Walkthrough

The example mod is the best executable reference for NexusCore. It is deliberately dense: real mods should split similar code across modules, but this sample keeps the relationships visible.

## Source Layout

```text
examples/example-mod/
  common/
    src/main/java/.../NexusCoreExampleContent.java
    src/main/java/.../NexusCoreExampleSystems.java
  fabric/
    src/main/java/.../NexusCoreExampleFabric.java
    src/main/java/.../NexusCoreExampleFabricDataGenerator.java
    src/main/resources/fabric.mod.json
  neoforge/
    src/main/java/.../NexusCoreExampleNeoForge.java
    src/main/resources/META-INF/neoforge.mods.toml
```

`NexusCoreExampleContent` is the mod. `NexusCoreExampleSystems` is the subsystem exercise suite. The loader modules only initialize NexusCore, bootstrap the common mod, and wire datagen.

## Bootstrap Flow

Fabric calls:

```java
NexusCore.init();
NexusCoreExampleContent.bootstrap();
```

NeoForge does the same from its `@Mod` constructor, then adds the datagen provider. The common class extends `NexusMod`, so bootstrap runs in this order:

1. Install lifecycle hooks.
2. Fire `PRE_INIT`.
3. Run `beforeRegistries`.
4. Initialize content modules.
5. Register every deferred registry in the mod registry group.
6. Run `onInitialize`.
7. Fire common lifecycle/validation phases.
8. Log startup diagnostics.

This order is important. Registry declarations belong in `beforeRegistries` or content modules. Runtime checks, debug sections, network diagnostics, and validation suites belong in `onInitialize`.

## Content Registration

The example creates a creative tab first so item and block builders can attach to it. The items demonstrate generated models, tooltips, food values, and fuel. The blocks demonstrate cube models, block items, loot, mining tags, required tool tags, map colors, and color metadata.

The sapphire block set demonstrates the higher-level material-family API:

```java
sapphireSet = NexusBlockSets.gem(MOD_ID, "sapphire")
        .material(MapColor.COLOR_BLUE)
        .strength(4.0F, 5.0F)
        .creativeTab(tab)
        .generateRecipes()
        .generateTags()
        .register();
```

Use block sets when several files and entries follow a repeatable convention. Use individual builders for blocks with custom behavior or custom assets.

## Config

`ExampleConfig` covers all option families:

- `machine_energy_cost`: ranged int, server-synced, world-reload metadata.
- `enable_particles`: boolean, client group metadata.
- `workbench_label`: non-blank string, server-synced UI label.
- `balance_mode`: enum option for machine balancing presets.

The example validates the config during initialization, exports schema metadata, creates a preset, applies a migration, and checks the owo bridge. The generated config screen comes from NexusCore's client module; the common example does not import screen classes.

## Datagen

`populateGeneratedData` is the single shared data plan. It writes:

- Translations
- Item and block models through builders
- Blockstates
- Loot tables
- Tags
- Recipes
- Advancements
- Worldgen JSON
- Typed resource JSON
- Sound subtitles
- Config labels
- Keybind labels
- Entity labels

Both loader datagen entrypoints consume this plan through `NexusDataProvider`.

## Components And Migration

The `mode` data component is persistent, network-synced, cacheable, and tooltip-aware. `NexusCoreExampleSystems.demonstrateComponents` then:

1. Creates an item stack before/after pair.
2. Sets the component on the after stack.
3. Uses `ComponentDebug.diff` to show the data change.
4. Migrates a legacy `CompoundTag` key with `NbtToComponentMigration`.
5. Shows codec helpers for enum-like persisted values.

This is the recommended pattern when updating older NBT-heavy item state to Minecraft 1.21.1 data components.

## Machines

The ruby press demonstrates the v1.2 machine model:

- `NexusMachineDefinition` describes the machine contract.
- `MachineRecipeDefinition` describes one recipe.
- `MachineProcessingEngine` performs the tick logic over inventory, energy, fluid, state, and recipes.
- `MachineState` stores progress, status, redstone mode, and side configuration.
- `MachineScreenLayout.generated` and `MachineUiBindings.machine` describe the client UI.

The example validates the machine immediately with `ValidationSuite`. That gives contributors a fast signal that the descriptor, recipe, inventory, energy, and fluid pieces still agree.

## Recipe Viewer Integration

The example registers a category and a two-page display. It intentionally includes advanced widgets:

- Item inputs and outputs
- Fluid input
- Animated progress arrow
- Catalyst slot
- Tooltip region
- JEI transfer button
- EMI recipe tree control
- REI custom button
- Multi-page fallback display

JEI, EMI, and REI do not expose identical widget APIs. NexusCore keeps the portable data model stable and applies viewer-specific controls only where the target viewer can support them.

## Events And Debug Sections

`NexusCoreExampleSystems.demonstrateEvents` registers a server tick listener and records event trace entries. The example exposes multiple debug sections so the in-game debug browser and commands can show state without attaching a debugger.

Typical sections:

- `nexuscore_example.machine`
- `nexuscore_example.validation`
- `nexuscore_example.systems`
- `nexuscore_example.datapack_loader`
- `nexuscore_example.registry_report`
- `nexuscore_example.benchmarks`

When adding a new subsystem to a mod, add one concise debug section for it.

## Networking

The example declares a main channel and a diagnostics channel. The diagnostics method demonstrates:

- Protocol versions
- Mismatch messages
- `NetworkMonitor`
- `RequestResponse`
- `SyncBatcher`
- `PacketTestHarness`

Use `PacketTestHarness` for payload encode/decode tests before connecting packets to actual handlers. Use `PacketGuards` in real handlers to validate side, player, and thread before changing state.

## Persistence And Player Data

The example demonstrates both generic and player-scoped state:

- `AttachmentKey` and `AttachmentStore` for typed attachment-style state.
- `DataMigrationChain` for versioned saved data migration.
- `NexusNbt` for optional, type-checked NBT reads.
- `PlayerDataStore` for UUID-indexed player data.
- `PlayerAttachmentSpec` for documenting sync/copy/client-write policy.

Use `AttachmentKey.CopyPolicy` and `PlayerAttachmentSpec.SyncPolicy` deliberately. They communicate save/sync semantics even when a backing implementation is simple.

## World, Entity, And Worldgen

The example registers a `ruby_marker` entity type using `NexusEntityDefinitions.registerType`. It also creates projectile metadata and a combat profile descriptor so docs, debug screens, and generated data can report entity intent.

Worldgen is declared with:

```java
NexusWorldgen.ore(MOD_ID, "ruby_ore")
        .state(MOD_ID + ":ruby_ore")
        .veinSize(6)
        .count(8)
        .heightRange(-32, 64)
        .writeTo(NexusData.plan(MOD_ID));
```

NeoForge receives generated biome modifier JSON. Fabric receives runtime biome modification through NexusCore's Fabric bridge.

## UI

The example covers both data-only UI descriptors and machine-specific bindings:

- `NexusUi.ScreenSpec`
- `WidgetLibrary`
- `WidgetDescriptor`
- `FormBuilder`
- `ObservableValue`
- `MachineUiBindings`
- `ScreenRouter`
- `HudOverlayRegistry`
- `MiniMarkup`
- `RichTextBuilder`

Common code should generally create descriptors and binding metadata. Actual Minecraft screen classes belong in client code.

## Performance

The performance section demonstrates:

- `DirtyFieldTracker`
- `LazyCache`
- `ReloadAwareMemoizedSupplier`
- `NexusRateLimiter`
- `ChunkTaskQueue`
- `BatchedBlockUpdates`
- `NamedProfiler`
- `BenchmarkSuite`
- `NexusBenchmarks`
- `NexusMath.WeightedTable`

The profiler and benchmark output is published through debug sections. This makes it easy to compare local changes without adding a separate profiler integration for every experiment.

## What To Copy

For a real mod:

- Copy the bootstrap shape from `NexusCoreExampleContent`.
- Copy specific subsystem snippets from `NexusCoreExampleSystems`.
- Split the copied snippets into separate files by ownership.
- Keep the debug-section habit.
- Keep datagen centralized enough that the generated data plan remains reviewable.
