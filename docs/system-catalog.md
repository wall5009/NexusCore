# NexusCore System Catalog

This catalog is the high-level map of NexusCore. It covers every public system and points to maintained docs, scaffolds, and GameTests for usage coverage. Use it when deciding which package to reach for, when reviewing release coverage, or when onboarding a new contributor.

## Reading Order

1. Start with `docs/getting-started.md` for project setup and bootstrap.
2. Read `docs/scaffolding-walkthrough.md` to generate focused examples for your own mod.
3. Use this catalog as the package-by-package reference.
4. Use the versioned `docs/v1.2/` and `docs/v1.3/` pages for systems added in those releases.

## Module And Loader Layout

| Module | Purpose | Owns |
| --- | --- | --- |
| `common` | Loader-neutral API and shared runtime logic | Builders, descriptors, registries, config, datagen plans, networking declarations, machines, inventory, transfer abstractions, diagnostics, tests |
| `fabric` | Fabric runtime bindings | Fabric entrypoint, Fabric datagen, Fabric GameTest entrypoint, Fabric Transfer bridge, Team Reborn Energy bridge, Fabric recipe viewer plugins, Fabric worldgen runtime bridge |
| `neoforge` | NeoForge runtime bindings | NeoForge entrypoint, NeoForge datagen, NeoForge GameTest registration, capabilities, NeoForge recipe viewer plugins |
| `nexus-gradle` | Build tooling | Scaffolding tasks, validation tasks, docs site tasks, ABI tasks, release metadata |

## Core

Package: `com.rollylindenshnizzer.nexuscore.core`

Use this layer for bootstrap, environment checks, identifiers, diagnostics, task queues, and common exception types.

Important classes:

- `NexusCore`: initializes NexusCore itself.
- `NexusMod`: optional base class for mods that want the NexusCore bootstrap sequence.
- `NexusLifecycle`: shared phases for pre-init, common init, common setup, and validation hooks.
- `NexusIds` and `ResourceLocationBuilder`: namespace/path validation and ID construction.
- `NexusEnvironment`: loader, side, dev, datagen, test, and mod-loaded checks.
- `NexusDiagnostics`: startup and subsystem reports.
- `CrashHints`: maps common exception text to actionable remediation.
- `NexusTasks` and `TaskQueue`: queues work for known lifecycle boundaries.
- `NexusVersion`: generated library version and dependency constants.

Example usage:

- `docs/scaffolding-walkthrough.md` shows how to generate focused code examples.
- `NexusCoreGameTestScenarios` contains compile-checked usage of public systems.
- `docs/v1.3/cookbook.md` gives copyable snippets for the newer systems.

Guidance:

- Register content in `beforeRegistries`.
- Do not call `RegistrySupplier.get()` for objects that require vanilla registry availability until after registration.
- Put client-only classes behind client entrypoints or reflective bridges.
- Use `NexusIds.id(modId, path)` instead of hand-parsing strings in public APIs.

## Registries And Content Modules

Packages: `registry`, `item`, `block`, `blockentity`, `entity`, `sound`, `menu`

Use this layer to create and organize registry entries while keeping duplicate checks and diagnostics centralized.

Important classes:

- `NexusRegistries`: obtains a `NexusRegistryGroup` per mod ID.
- `NexusRegistryGroup`: wraps Architectury deferred registers for items, blocks, block entities, menus, entity types, sounds, particles, creative tabs, recipe types, recipe serializers, and data components.
- `ContentModule` and `ContentModuleManager`: group larger mods into dependency-sorted registration modules.
- `NexusContentManifest`: records registered and generated content for debug, docs, and validation.
- `NexusRegistryReports`: creates count summaries for registry groups.
- `CustomRegistrySpec`: documents custom data registries that a mod owns.

Example usage:

- `scaffolded registry code` registers the creative tab, items, blocks, data component, block set, and machine.
- `NexusCoreGameTestScenarios` demonstrates dependency-sorted modules.
- `NexusCoreGameTestScenarios` records group metadata, menu type, sounds, entity type, custom registry spec, and tags.

Guidance:

- Prefer high-level builders such as `NexusItems.item` and `NexusBlocks.block` for ordinary content.
- Use the raw registry group for content types without a higher-level builder.
- Use content modules to separate materials, machines, worldgen, client descriptors, and compatibility in large mods.
- Run registry reports in debug output to catch unexpected missing entries early.

## Items, Blocks, Block Sets, And Assets

Packages: `item`, `block`, `loot`, `tag`, `advancement`

Use this layer to define everyday mod content and generated data.

Important classes:

- `ItemBuilder`, `NexusItem`, `NexusItems`, `FoodBuilder`, `ArmorMaterialBuilder`, `SimpleTier`, `TooltipBuilder`, `ItemInteractionBuilder`.
- `BlockBuilder`, `NexusBlocks`, `NexusBlockSets`, `NexusBlockSet`, `NexusBlockStates`, `NexusShapes`, `BlockInteractionBuilder`, `RedstoneHelpers`.
- `LootTableBuilder`, `NexusTags`, `AdvancementJsonBuilder`.

Example usage:

- `ruby`, `raw_ruby`, and `ruby_apple` demonstrate generated item models, tooltips, food, and fuel.
- `ruby_block` and `ruby_ore` demonstrate block item generation, cube models, self-drop loot, mining tags, tool level tags, strength, map color, and colors.
- `sapphireSet` demonstrates a generated gem block set with recipes and tags.
- `NexusCoreGameTestScenarios` emits extra tags and loot through lower-level builders.

Guidance:

- Put translation keys in the data plan even when the item builder can infer some names. Explicit keys make generated docs and language reviews easier.
- Use block sets for repeated material families, not one-off special blocks.
- Use interaction builders when item/block behavior is rule-based and can be expressed as small predicates.

## Components And Persistence

Packages: `component`, `persistence`, `player`

Use this layer for modern item data, saved data migration, attachment-style state, and player-scoped state.

Important classes:

- `NexusComponents`, `NexusComponentBuilder`, `NexusComponentSpec`, `ComponentCopyStrategy`, `ComponentDebug`, `NbtToComponentMigration`.
- `NexusDataComponents`, `NexusCodecs`, `NexusNbt`, `DataMigrationChain`, `AttachmentKey`, `AttachmentStore`.
- `PlayerAttachmentSpec`, `PlayerDataStore`, `NexusPlayers`, `PermissionHelpers`.

Example usage:

- `modeComponent` is a persistent, network-synced item component with default metadata and tooltip behavior.
- `NexusCoreGameTestScenarios` compares component maps and migrates a legacy NBT key into the component.
- `NexusCoreGameTestScenarios` uses attachment keys, copy policies, saved-data migration, NBT helpers, and player data copying.

Guidance:

- Prefer data components for item state in Minecraft 1.21.1.
- Keep legacy NBT migration code explicit and testable.
- Model player attachments with sync/copy metadata even when the current implementation stores the data in a simple backing map.

## Config

Package: `config`

Use this layer for typed configuration, validation metadata, schema export, config dependency analysis, migrations, presets, sync metadata, and generated owo config screens.

Important classes:

- `NexusConfig`, `ConfigOption`, `IntOption`, `BooleanOption`, `StringOption`, `EnumOption`.
- `ConfigSchemaExporter`, `ConfigSerializer`, `ConfigPreset`, `ConfigMigration`, `ConfigDependencyGraph`, `ConfigSyncDiagnostics`, `NexusConfigRegistry`, `OwoConfigBridge`.
- Client screen classes under `client.config`.

Example usage:

- `ExampleConfig` defines int, boolean, string, and enum options.
- `ConfigSchemaExporter.jsonSchema(config, "1.1")` exports schema metadata.
- `NexusCoreGameTestScenarios` creates a preset, applies a migration, and verifies owo bridge availability.

Guidance:

- Call `validateAll()` during initialization.
- Use groups, comments, and translation keys. The generated screen and docs rely on this metadata.
- Mark server-synced options explicitly.
- Use config migrations for renamed or newly required values.

## Data Generation And Data Validation

Package: `data`

Use this layer to build generated assets/data from common code and validate the output before publishing.

Important classes:

- `NexusData` and `NexusData.DataPlan`: common generated data plan.
- `NexusDataProvider`: vanilla datagen provider that writes a plan.
- `NexusDataValidator`, `DataValidationReport`, `DatagenReportWriters`.
- `RecipeJsonBuilder`, `RecipeDiagnostics`, `IncrementalDatagen`, `DatagenBootstrap`.

Example usage:

- `scaffolded datagen code` writes translations, recipes, worldgen configured feature JSON, and advancements.
- `NexusCoreGameTestScenarios` adds tags, loot, subtitles, keybind translations, config translations, and typed data JSON.
- Fabric and NeoForge example datagen providers both consume the same data plan.

Guidance:

- Keep generated JSON paths relative to `assets/<modid>` or `data/<modid>`.
- Run datagen for both loaders when loader-specific output exists.
- Check `nexus.content.json` to verify that content and generated resources were recorded.
- Treat datagen warnings as release-review items, not harmless noise.

## Resources And Datapack Loading

Package: `resource`

Use this layer when mods need their own datapack-driven JSON types.

Important classes:

- `JsonSchema`: required/optional primitive shape validation.
- `TypedDataLoader<T>`: folder plus schema plus JSON decoder.
- `DataDrivenRegistry<T>`: reloads typed values from datapack JSON maps.
- `ResourceValidationReport`: loaded/error/warning summary.
- `NexusResources`: safe JSON and filesystem helpers.

Example usage:

- `NexusCoreGameTestScenarios` loads a `ruby_traits/polished_ruby` entry through a schema-backed typed loader.
- `NexusCoreGameTestScenarios` emits the matching JSON.

Guidance:

- Start with schema validation for fast, readable errors.
- Convert JSON into records or immutable descriptors before runtime use.
- Expose validation summaries through debug sections.

## Machines, Inventory, Energy, And Fluids

Packages: `machine`, `inventory`, `energy`, `fluid`, `ui.binding`

Use this layer for processing machines, side-aware transfer, inventory routing, generated machine UI, and testable state transitions.

Important classes:

- Machine: `NexusMachineDefinition`, `NexusMachines`, `MachineRecipeDefinition`, `MachineProcessingEngine`, `MachineState`, `MachineScreenLayout`, `BaseMachineBlockEntity`, `MachineUpgrade`, `SideConfiguration`, `RedstoneControlMode`.
- Inventory: `SimpleItemHandler`, `SlotRange`, `SlotGroup`, `SlotRole`, `QuickMoveRouter`, `TransferRule`, `InventoryTransfer`, `InventorySnapshot`, `InventoryDropPolicy`.
- Energy: `NexusEnergyStorage`, `NexusEnergyTransfer`, `EnergyAccess`, legacy `EnergyStorage` and `EnergyTransfer`.
- Fluid: `NexusFluidTank`, `NexusFluidTransfer`, `FluidAccess`, `FluidStack`, legacy `FluidTank`.
- UI binding: `MachineUiBindings`, `ObservableValue`.

Example usage:

- `rubyPressDefinition` declares energy capacity, fluid capacity, slot groups, generated screen, and category.
- `rubyPressRecipe` consumes redstone and water to produce a diamond in the scaffolded coverage.
- `MachineProcessingEngine` is ticked in `onInitialize` and verified by `ValidationSuite`.
- `NexusCoreGameTestScenarios`, `demonstrateInventory`, and `demonstrateMachineUi` show the supporting APIs.

Guidance:

- Keep machine descriptors static and small; keep world-specific state in block entities or state objects.
- Use transfer simulation before mutating storage.
- Name slot groups by role, then derive menu routes from those groups.
- Put machine UI labels and widget descriptors in data/configurable metadata where possible.

## Recipe Viewers And Compatibility

Packages: `compat`, `compat.recipeviewer`, loader-specific JEI/EMI/REI plugins

Use this layer for optional integrations and recipe viewer displays.

Important classes:

- `NexusCompat`, `CompatModule`, `CompatModuleLoader`, `IntegrationDescriptor`, `SafeClassloading`.
- `RecipeViewerBridge`, `RecipeViewerCategory`, `RecipeViewerDisplay`, `RecipeViewerDisplayPage`, `RecipeViewerWidget`, `RecipeViewerLayout`.
- Widget types for item slots, fluids, text, tooltips, progress, advanced controls, and viewer support matrices.

Example usage:

- `scaffolded content.registerRecipeViewerContent` creates one portable category and a two-page display.
- The first page includes fluid input, animated progress, item catalyst, JEI transfer button, EMI recipe tree control, REI info button, and tooltip areas.
- The second page demonstrates multi-page display fallback.

Guidance:

- Keep core recipe data portable.
- Use viewer-specific advanced controls only when they improve the experience; always provide a portable fallback.
- Declare JEI, EMI, and REI as optional dependencies in metadata.

## Client Runtime, Screens, And UI

Packages: `client`, `client.config`, `client.debug`, `client.machine`, `ui`, `ui.form`, `ui.nav`

Use this layer for descriptors that common code can produce safely and runtime classes that client entrypoints install.

Important classes:

- `NexusClientDescriptors`, `ClientDescriptor`, `ClientEffectSpec`, `ClientEffectRegistry`, `ClientEffectRuntime`, `NexusCoreClient`.
- `NexusConfigScreen`, `NexusConfigScreens`, `NexusDebugScreen`, `NexusProfilerHud`, `NexusMachineScreen`, `NexusMachineScreens`.
- `NexusUi`, `WidgetLibrary`, `WidgetDescriptor`, `FormBuilder`, `ScreenRouter`, `HudOverlayRegistry`, `MiniMarkup`, `RichTextBuilder`.

Example usage:

- `NexusCoreGameTestScenarios` registers common-safe client descriptors.
- `NexusCoreGameTestScenarios` builds screen specs, widgets, form validation, router state, mini-markup, rich text, HUD overlays, and machine UI bindings.

Guidance:

- Common code may create descriptors.
- Client-only runtime registration belongs behind client entrypoints.
- Use `OwoConfigBridge` when common code needs to request a config screen without importing client classes.

## Networking, Menus, And Server Authority

Packages: `network`, `menu`, `security`

Use this layer for versioned protocols, packet diagnostics, packet tests, request/response flows, menu metadata, and server-authoritative checks.

Important classes:

- `NexusNetworking`, `NetworkMonitor`, `PacketGuards`, `PacketTestHarness`, `RequestResponse`, `SyncBatcher`, `SyncProfile`.
- `NexusMenus`, `MenuBinding`, `MenuDebugInfo`.
- `ServerAuthority`, `ClientActionGuard`, `SafePaths`.

Example usage:

- `scaffolded content` declares a `main` channel with a protocol version and mismatch message.
- `NexusCoreGameTestScenarios` creates a diagnostic channel, records network stats, tests a packet round trip, queues a sync batch, and runs request/response.
- `NexusCoreGameTestScenarios` validates a client action and safe export path.

Guidance:

- Check side, player, dimension, distance, and permissions at packet boundaries.
- Version every logical protocol.
- Use `PacketTestHarness` for payload round-trip tests outside Minecraft runtime.

## Events, Debugging, And Doctor Reports

Packages: `event`, `debug`, `command`

Use this layer to observe runtime behavior, expose diagnostics, and make errors actionable.

Important classes:

- `NexusEvents`, `EventDiagnosticRegistry`, `EventTrace`.
- `DebugRegistry`, `NexusDoctor`, `DoctorReport`, `CrashHintClassifier`, `DebugInspector`, `ReportExporter`, `ErrorReference`.
- `NexusCommands`, `NexusDebugCommands`, `NexusCoreCommands`, `CommandSuggestions`.

Example usage:

- The scaffolded coverage registers the `/nexus` command and installs debug commands.
- Debug sections report items, blocks, machine state, validation, system snapshots, datapack loader status, benchmark output, registry report, and entity registration.
- `NexusCoreGameTestScenarios` registers a server tick listener and records event traces.

Guidance:

- Every subsystem should expose a short debug summary.
- Use doctor reports for release gates and support bundles.
- Use crash hint classification to turn common stack traces into fix directions.

## Entity, World, And Worldgen

Packages: `entity`, `world`, `worldgen`

Use this layer for entity descriptors, projectile metadata, combat profiles, world helpers, and generated/runtime worldgen.

Important classes:

- Entity: `EntityTypeBuilder`, `NexusEntities`, `NexusEntityDefinition`, `NexusEntityDefinitions`, `RegisteredNexusEntity`, `ProjectileDefinition`, `ProjectileHelpers`, `CombatProfile`, `DamageHelpers`, `GoalBuilder`.
- World: `NexusWorlds`, `RaycastBuilder`, `TeleportTargetBuilder`, `StructureHelpers`.
- Worldgen: `NexusWorldgen`, `OreGenerationBuilder`, `BiomeSelector`, `OreFeatureJsonBuilder`, `PlacedFeatureJsonBuilder`, `WorldgenJson`, `WorldgenValidationReport`.

Example usage:

- `ruby_marker` is registered as an entity type through `NexusEntityDefinitions.registerType`.
- `NexusWorldgen.ore` describes the ruby ore feature.
- `NexusCoreGameTestScenarios` uses radius iteration, structure center helpers, teleport target descriptors, and combat profiles.

Guidance:

- Use descriptors for generated assets and docs.
- Use registration helpers when the library can wire the type; provide the actual entity factory from mod code.
- Keep loader-specific worldgen differences inside NexusCore bridges or generated JSON.

## Performance And Testing

Packages: `performance`, `test`, `math`

Use this layer for cheap runtime helpers, profiling, benchmarks, assertions, GameTest scenarios, and pure-Java validation.

Important classes:

- Performance: `NamedProfiler`, `NexusProfilerHud`, `BenchmarkSuite`, `BenchmarkCase`, `BenchmarkResult`, `NexusBenchmarks`, `CooldownTracker`, `ExpiringCache`, `LazyCache`, `ReloadAwareMemoizedSupplier`, `NexusRateLimiter`, `DirtyFieldTracker`, `ChunkTaskQueue`, `BatchedBlockUpdates`.
- Testing: `ValidationSuite`, `NexusAssertions`, `GameTestSpec`, `GoldenFiles`, `FakeInventory`, `NexusCoreGameTestScenarios`.
- Math: `NexusMath`, `WeightedTable`.

Example usage:

- `ValidationSuite` verifies the scaffolded coverage machine output.
- `NexusCoreGameTestScenarios` records profiler sections, runs a benchmark, exercises caches, queues chunk work, marks block updates, rate-limits an action, and uses a weighted table.
- Fabric and NeoForge GameTests run the shared `NexusCoreGameTestScenarios`.

Guidance:

- Put pure logic in validation suites or unit-style tests.
- Put Minecraft runtime behavior in GameTests.
- Keep profiler sections named by subsystem, for example `example.machine.tick`.

## Release Checklist

Before publishing a NexusCore update or a mod built on it:

- Build Fabric and NeoForge jars.
- Run datagen for both loaders.
- Run `runNexusValidation`.
- Run Fabric and NeoForge GameTests.
- Run binary compatibility checks against the declared baseline.
- Inspect generated docs and `nexus.content.json`.
- Confirm optional integrations degrade cleanly when JEI, EMI, REI, owo-lib, Fabric Transfer, Team Reborn Energy, or NeoForge capabilities are absent.
- Confirm no client-only classes load from dedicated server paths.
