# NexusCore

NexusCore is a Minecraft 1.21.1 Architectury library mod for Fabric and NeoForge. It provides practical APIs for registry setup, item and block builders, datagen, config validation, owo-powered UI/debugging, networking, machines, inventories, energy, fluids, recipe viewer integrations, GameTests, diagnostics, and general modding utilities.

## Modules

- `common`: shared public API.
- `fabric`: Fabric runtime, Fabric datagen, Fabric GameTest entrypoint, Team Reborn Energy bridge, Fabric Transfer bridge, and Fabric JEI/EMI/REI plugins.
- `neoforge`: NeoForge runtime, NeoForge datagen, NeoForge GameTest registration, NeoForge capabilities, and NeoForge JEI/EMI/REI plugins.
- `examples/example-mod/common`: shared example mod content.
- `examples/example-mod/fabric`: Fabric example loader module.
- `examples/example-mod/neoforge`: NeoForge example loader module.

## What It Offers

- Lifecycle: `NexusCore`, `NexusMod`, lifecycle phases, diagnostics, side-safe helpers, IDs, task queues, and friendly exceptions.
- Registries: registry groups, content modules, creative tab builder, duplicate validation, custom registry specs, and conditions.
- Content builders: items, blocks, block entities, entities, sounds, menus, armor materials, tiers, food, tooltips, interactions, and blockstate helpers.
- Datagen: generated language, item models, block models, blockstates, loot tables, tags, recipes, advancements, and worldgen JSON.
- Config: typed options, validation, scopes, reload/sync metadata, presets, migrations, import/export, registry discovery, and generated owo config editor screens.
- UI and debug: owo debug browser, HUD overlay registry, mini-markup, rich text descriptors, debug registry, commands, and report export.
- Runtime systems: networking, packet guards, persistence/codecs/NBT, data components, attachments, player/world helpers, raycasts, teleport targets, math, performance helpers, machines, energy, fluids, inventories, and recipe caches.
- Compatibility: concrete JEI, EMI, and REI plugins with custom recipe layouts, fluids, animated progress, multi-page displays, and viewer-specific controls, plus Fabric Team Reborn Energy and NeoForge capability adapters.
- Testing: validation suites, assertions, fake inventories, registered Fabric and NeoForge GameTests, and bundled test structures.

## Build

```powershell
.\gradlew.bat build
```

Main jars are produced under `fabric/build/libs/`, `neoforge/build/libs/`, and `build/forgix/`. Example jars are produced under `examples/example-mod/fabric/build/libs/` and `examples/example-mod/neoforge/build/libs/`.

## Datagen

```powershell
.\gradlew.bat :fabric:runDatagen
.\gradlew.bat :neoforge:runDatagen
.\gradlew.bat :example-fabric:runDatagen
.\gradlew.bat :example-neoforge:runDatagen
```

## GameTest

```powershell
.\gradlew.bat :fabric:runGametest
.\gradlew.bat :neoforge:runGametest
```

## Documentation

Start with:

- `docs/getting-started.md`
- `docs/architecture.md`
- `docs/compatibility-guide.md`
- `docs/testing-guide.md`

The rest of `docs/` covers registries, content builders, datagen, config, machines, networking, debugging, and worldgen.
