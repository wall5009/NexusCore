# NexusCore Example Mod

This example is a dual-loader reference mod for NexusCore. It is intentionally broader than a normal tutorial mod: the goal is to give each NexusCore system at least one concrete, compile-checked usage site that can be copied into real projects.

The shared implementation lives in `common/src/main/java/com/rollylindenshnizzer/nexuscore/example/`:

- `NexusCoreExampleContent` is the actual mod bootstrap. It registers items, blocks, config, components, machines, worldgen, recipe viewer content, commands, networking, validation, and debug sections.
- `NexusCoreExampleSystems` is the subsystem tour. It exercises the APIs that are usually hard to demonstrate through a single content item: content modules, custom registry descriptors, data-driven registries, events, UI descriptors, persistence, player/world helpers, security guards, benchmarks, profiler data, packet test harnesses, and generated reports.
- `fabric` and `neoforge` contain only the loader entrypoints and datagen hooks. They call the same shared bootstrap so the mod behaves the same on both loaders.

## Build And Run

Build both loader jars from the repository root:

```powershell
.\gradlew.bat :example-fabric:build
.\gradlew.bat :example-neoforge:build
```

Run datagen for both loaders:

```powershell
.\gradlew.bat :example-fabric:runDatagen
.\gradlew.bat :example-neoforge:runDatagen
```

Generated output is written to each loader module's `src/generated/resources` folder. The generated files include language entries, item models, block models, blockstates, loot tables, recipes, tags, worldgen features, NeoForge biome modifiers, typed resource examples, and the `nexus.content.json` manifest.

## What The Example Registers

The content bootstrap registers a small ruby-themed mod:

- Creative tab: `nexuscore_example:main`
- Items: `ruby`, `raw_ruby`, `ruby_apple`
- Blocks: `ruby_block`, `ruby_ore`
- Generated block set: `sapphire_block`, `sapphire_ore`
- Data component: `mode`
- Machine definition: `ruby_press`
- Machine recipe definition: `ruby_pressing`
- Worldgen ore definition: `ruby_ore`
- Entity definition and registered entity type: `ruby_marker`
- Menu type: `tutorial_chest_menu`
- Sound events: `ruby_press/start`, `ruby_press/finish`
- Client descriptors: keybind, debug layer, render layer, color provider, particle effect
- Recipe viewer category and multi-page display: `ruby_workbench`

## System Coverage Map

Use this section as the quickest way to find a real usage site.

| System | Example Entry Point | What It Demonstrates |
| --- | --- | --- |
| Bootstrap and lifecycle | `NexusCoreExampleContent.bootstrap`, `NexusMod.init` | Common bootstrap, registry timing, lifecycle callbacks, diagnostics |
| Content modules | `NexusCoreExampleSystems.contentModules` | Module dependency ordering, module data hooks, compatibility hooks |
| Registries | `beforeRegistries`, `registerBeforeRegistries` | Registry groups, creative tabs, item/block/menu/sound/entity/component registration, duplicate-safe paths |
| Items and blocks | `beforeRegistries` | Item builder, food builder, fuel values, block builder, block item generation, cube models, loot, mining tags |
| Block sets | `sapphireSet` registration | Generated storage/ore blocks, generated recipes, generated tags, shared creative tab |
| Data components | `modeComponent`, `demonstrateComponents` | Persistent codec, network codec, defaults, tooltip metadata, component diffing, NBT migration |
| Config | `ExampleConfig`, `demonstrateConfig` | Int, boolean, string, enum options, validation, sync metadata, groups, comments, presets, migrations, owo bridge status |
| Datagen | `populateGeneratedData` | Translations, recipes, advancements, loot, tags, item models, block models, worldgen JSON, typed resource JSON |
| Content manifest | generated `nexus.content.json` | Registry and generated data discovery for diagnostics and docs |
| Machines | `rubyPressDefinition`, `rubyPressRecipe` | Slot groups, energy/fluid capacity, side IO, processing engine, machine state, generated screen layout |
| Machine UI | `demonstrateMachineUi` | Generated `MachineScreenLayout`, `MachineUiBindings`, progress/energy/fluid/redstone/side widgets |
| Inventory | `machineInventory`, `demonstrateInventory` | `SimpleItemHandler`, transfer rules, transfer traces, snapshots, diffing, drop policies |
| Energy | `energy`, `demonstrateTransfers` | Side-aware storage, insert/extract simulation, transfer between storages |
| Fluid | `tank`, `demonstrateTransfers` | Side-aware tanks, one-fluid storage, fill/drain simulation, tank transfer |
| Recipe viewers | `registerRecipeViewerContent` | JEI/EMI/REI portable category/display registration, fluids, animated arrows, multi-page displays, viewer controls |
| Networking | `NexusNetworking.channel`, `demonstrateNetworking` | Versioned channels, mismatch messages, packet diagnostics, request/response, batching, packet round-trip testing |
| Commands | `NexusCommands.literal`, `NexusDebugCommands.install` | Common command registration and debug command installation |
| Debug and Doctor | debug sections, `demonstrateCoreAndDiagnostics` | Live debug registry, crash hints, doctor reports, datagen validation summaries, registry reports |
| Events | `demonstrateEvents` | Safe event registration, event diagnostic marks, bounded event trace |
| Resource loaders | `demonstrateDataAndResources` | `TypedDataLoader`, `JsonSchema`, `DataDrivenRegistry`, validation reports |
| Worldgen | `NexusWorldgen.ore` | Ore descriptor, configured feature/placed feature/biome modifier datagen, Fabric runtime biome modification |
| Entities | `ruby_marker`, `CombatProfile` | Entity descriptors, registered entity type helper, projectile metadata, combat profile descriptor |
| Client descriptors | `registerBeforeRegistries` | Keybind/debug/render/color/particle descriptors without loading client-only classes from common code |
| UI descriptors | `demonstrateUi` | `NexusUi`, `WidgetLibrary`, form builder, rich text, mini-markup, screen router, HUD overlay registry |
| Persistence | `demonstratePersistence` | Attachment keys/stores, copy policies, saved-data migrations, NBT helpers, player data store, player attachment specs |
| Player/world helpers | `demonstratePlayerWorldSecurity` | Safe path validation, radius iteration, structure center helpers, teleport target descriptors |
| Security | `ClientActionGuard`, `SafePaths` | Client action allow-lists and safe report/export paths |
| Performance | `demonstratePerformance` | Dirty fields, lazy caches, reload-aware memoization, chunk task queues, block update batches, rate limiting, profiler sections, benchmarks |
| Testing | `ValidationSuite`, `PacketTestHarness` | Runtime validation checks and packet serialization tests |

## Walkthrough

Start in `NexusCoreExampleContent.beforeRegistries`. This method shows the normal "register content first, then let `NexusMod` register the group" flow. The ruby items and blocks use high-level builders, while the sapphire block set shows a higher-level generated content bundle.

The config object is constructed once as part of the mod instance. It registers itself with `NexusConfigRegistry`, exports schema metadata, validates values on initialization, and feeds the example machine's starting energy cost. The generated owo config screen is supplied by NexusCore itself, so the example only defines typed options and metadata.

The machine path has three pieces:

- `NexusMachineDefinition` describes capacity, slot groups, side behavior, upgrades, generated UI, comparator output, and redstone defaults.
- `MachineRecipeDefinition` describes the actual processing recipe: item input, fluid input, item output, energy cost, ticks, group, and category.
- `MachineProcessingEngine` consumes the definition, inventory, energy storage, fluid tank, state object, and available recipe list.

The recipe viewer path is deliberately more complex than a shaped recipe. It creates one portable category and a display with two pages. The first page includes an item input, fluid input, animated progress, output, catalyst, tooltip region, JEI transfer button, EMI control, and REI control. The second page demonstrates multi-page layout and fallback controls.

`NexusCoreExampleSystems` then layers in every subsystem that does not need to own a new content type. The methods are grouped by concern: diagnostics, config, resources, events, transfers, inventory, machine UI, components, networking, persistence, world/security, UI, and performance.

## Datagen Expectations

After running datagen, inspect these generated files:

- `assets/nexuscore_example/lang/en_us.json`
- `assets/nexuscore_example/models/item/ruby.json`
- `assets/nexuscore_example/models/block/ruby_ore.json`
- `assets/nexuscore_example/blockstates/ruby_ore.json`
- `data/nexuscore_example/recipe/ruby_block.json`
- `data/nexuscore_example/recipe/ruby_from_smelting.json`
- `data/nexuscore_example/loot_table/blocks/ruby_ore.json`
- `data/nexuscore_example/tags/items/gems/ruby.json`
- `data/nexuscore_example/tags/blocks/ores/ruby.json`
- `data/nexuscore_example/worldgen/configured_feature/ruby_ore.json`
- `data/nexuscore_example/worldgen/placed_feature/ruby_ore.json`
- `data/nexuscore_example/neoforge/biome_modifier/ruby_ore.json`
- `data/nexuscore_example/ruby_traits/polished_ruby.json`
- `data/nexuscore_example/nexus.content.json`

Fabric and NeoForge share the same data plan. NeoForge receives biome modifier JSON because that is the loader-native way to inject features. Fabric uses the runtime biome modification bridge in NexusCore for the same ore descriptor.

## Debugging In Game

Install and run the example with NexusCore. The debug command namespace is available through `NexusDebugCommands.install(MOD_ID)`, and the example contributes sections such as:

- `nexuscore_example.items`
- `nexuscore_example.blocks`
- `nexuscore_example.machine`
- `nexuscore_example.validation`
- `nexuscore_example.systems`
- `nexuscore_example.datapack_loader`
- `nexuscore_example.benchmarks`
- `nexuscore_example.registry_report`
- `nexuscore_example.entity`

These sections are intentionally compact strings so they work in commands, debug screens, and exported reports.

## Loader Notes

Fabric:

- Entry point: `NexusCoreExampleFabric`
- Datagen entry point: `NexusCoreExampleFabricDataGenerator`
- Metadata suggests JEI, EMI, REI, and Team Reborn Energy.
- Runtime content is shared through `example-common`.

NeoForge:

- Entry point: `NexusCoreExampleNeoForge`
- Datagen provider is added from `GatherDataEvent`
- Metadata declares optional JEI, EMI, and REI dependencies.
- Runtime content is shared through `example-common`.

## How To Copy This Into A Real Mod

Use `NexusCoreExampleContent` as the registration template and copy only the systems you need from `NexusCoreExampleSystems`. For production code, split large groups into real classes such as `ExampleItems`, `ExampleMachines`, `ExampleNetworking`, and `ExampleDatagen`. The example keeps many systems in one place because it is a reference map, not an architectural recommendation for a large mod.
