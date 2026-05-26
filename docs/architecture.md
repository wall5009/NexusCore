# Architecture

NexusCore is split by responsibility:

- `common`: loader-neutral public API, builders, data plans, config, diagnostics, recipe viewer descriptors, test helpers, machine utilities, and UI descriptors.
- `fabric`: Fabric entrypoints, Fabric datagen, Fabric GameTest entrypoint, Team Reborn Energy bridge, Fabric Transfer fluid bridge, and Fabric JEI/EMI/REI plugins.
- `neoforge`: NeoForge entrypoint, NeoForge datagen, NeoForge GameTest registration, NeoForge energy/fluid capability adapters, and NeoForge JEI/EMI/REI plugins.
- `examples/example-mod/*`: a real dual-loader mod consuming the library from shared code.

## Initialization Flow

`NexusCore.init()` installs shared lifecycle hooks and logs diagnostics. `NexusMod.init()` adds a mod-level sequence:

1. Guard against duplicate init.
2. Install Architectury lifecycle hooks.
3. Fire `PRE_INIT`.
4. Run `beforeRegistries()`.
5. Initialize content modules.
6. Register the mod's registry group.
7. Run `onInitialize()`.
8. Fire `COMMON_INIT` and validation phases.
9. Log startup diagnostics.

This keeps registry declarations separate from runtime setup.

## Loader Boundaries

Common code never imports Fabric-only or NeoForge-only APIs. Loader modules adapt NexusCore abstractions to the active platform:

- Fabric energy: `FabricTransferBridges.energy` returns Team Reborn `EnergyStorage`.
- Fabric fluids: `FabricTransferBridges.fluid` returns Fabric Transfer `Storage<FluidVariant>`.
- NeoForge energy: `NeoForgeCapabilities.energy` returns `IEnergyStorage`.
- NeoForge fluids: `NeoForgeCapabilities.fluid` returns `IFluidHandler`.

## Optional Integrations

Recipe viewer support has two layers:

- Common mods register categories and displays through `RecipeViewerBridge`.
- Loader modules expose those displays to JEI, EMI, and REI through real plugin classes.

The optional viewer mods are not required to load NexusCore. They are compile-only in Gradle and optional/suggested in metadata.

## owo-lib

owo-lib is a real dependency, not a placeholder. Common code compiles against `io.wispforest:owo-lib`, Fabric runtime requires `owo`, and NeoForge runtime requires `owo-lib-neoforge` plus its runtime libraries. The debug screen uses owo UI classes directly; the config bridge detects owo availability and gives config systems a stable integration point.
