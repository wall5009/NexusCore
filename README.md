# NexusCore

NexusCore is a Minecraft 1.21.1 Architectury library mod for Fabric and NeoForge. Version 1.3 focuses on larger mod systems: dimensions, portals, structures, biome/environment helpers, advanced entity AI descriptors, automation networks, data-driven definition registries, multiblocks, rituals, progression, authoring tools, simulation dashboards, balance tools, live reload safety, expanded diagnostics, scaffolding, and GameTests.

## Modules

- `common`: shared public API.
- `fabric`: Fabric runtime, Fabric datagen, Fabric GameTest entrypoint, Team Reborn Energy bridge, Fabric Transfer bridge, and Fabric JEI/EMI/REI plugins.
- `neoforge`: NeoForge runtime, NeoForge datagen, NeoForge GameTest registration, NeoForge capabilities, and NeoForge JEI/EMI/REI plugins.
- `nexus-gradle`: optional Gradle plugin with scaffolding and validation helpers.

## What It Offers

- Lifecycle: `NexusCore`, `NexusMod`, lifecycle phases, diagnostics, side-safe helpers, IDs, task queues, and friendly exceptions.
- Registries: registry groups, content modules, creative tab builder, duplicate validation, custom registry specs, and conditions.
- Content builders: items, blocks, block entities, entities, sounds, menus, armor materials, tiers, food, tooltips, interactions, and blockstate helpers.
- Datagen: generated language, item models, block models, blockstates, loot tables, tags, recipes, advancements, and worldgen JSON.
- Config: typed options, validation, scopes, reload/sync metadata, presets, migrations, import/export, registry discovery, and generated owo config editor screens.
- UI and debug: owo debug browser, HUD overlay registry, mini-markup, rich text descriptors, debug registry, commands, and report export.
- Runtime systems: networking, packet guards, persistence/codecs/NBT, data components, attachments, player/world helpers, raycasts, teleport targets, math, performance helpers, machine processing, side-aware energy/fluid transfer, inventory transfer rules, worldgen descriptors, entity descriptors, dimensions, structures, biomes, AI goals, automation graphs, data definition registries, and typed datapack loaders.
- Compatibility: concrete JEI, EMI, and REI plugins with custom recipe layouts, fluids, animated progress, multi-page displays, and viewer-specific controls, plus Fabric Team Reborn Energy and NeoForge capability adapters.
- Testing: validation suites, assertions, fake inventories, registered Fabric and NeoForge GameTests, and bundled test structures.
- v1.3 workflow: optional Gradle plugin, machine/transfer/worldgen/entity/datapack/dimension/structure/biome/AI/automation/data-definition/multiblock/ritual/progression/compatibility/balance templates, `/nexus` diagnostics, API reports, release metadata, docs site generation, ABI checks, and CI validation.

## Build

Run Gradle itself on Java 21. The Java toolchain also targets 21, but Architectury's production transformer currently fails under Java 25 in this project.

```powershell
.\gradlew.bat build
```

Main jars are produced under `fabric/build/libs/`, `neoforge/build/libs/`, and `build/forgix/`. The embedded example mod was removed; examples now live in docs, Gradle scaffolds, and GameTest scenarios.

## Datagen

```powershell
.\gradlew.bat :fabric:runDatagen
.\gradlew.bat :neoforge:runDatagen
```

## GameTest

```powershell
.\gradlew.bat :fabric:runGametest
.\gradlew.bat :neoforge:runGametest
```

## Documentation

Start with:

- `docs/getting-started.md`
- `docs/system-catalog.md`
- `docs/scaffolding-walkthrough.md`
- `docs/v1.3/index.md`
- `docs/v1.3/advanced-creation-systems.md`
- `docs/v1.3/dimensions-structures-biomes.md`
- `docs/v1.3/ai-automation-data.md`
- `docs/v1.3/recipes-compat-ui-live.md`
- `docs/v1.3/balance-testing-diagnostics-performance-security.md`
- `docs/v1.3/gradle-scaffolding.md`
- `docs/v1.3/cookbook.md`
- `docs/v1.2/getting-started.md`
- `docs/v1.2/machines.md`
- `docs/v1.2/transfer-energy-fluid.md`
- `docs/v1.2/client-runtime.md`
- `docs/architecture.md`
- `docs/compatibility-guide.md`
- `docs/testing-guide.md`

The rest of `docs/` covers registries, content builders, datagen, config, machines, networking, debugging, worldgen, compatibility, testing, release workflows, and scaffolded examples.
