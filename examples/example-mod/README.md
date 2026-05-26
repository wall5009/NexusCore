# NexusCore Example Mod

This is a full dual-loader example mod using NexusCore.

## Layout

- `common`: shared content, config, recipe viewer descriptors, data plans, debug sections, energy/fluid/inventory examples, and validation suite usage.
- `fabric`: Fabric entrypoint and Fabric datagen entrypoint.
- `neoforge`: NeoForge `@Mod` entrypoint and `GatherDataEvent` provider.

## What It Demonstrates

- `NexusMod` bootstrap.
- Creative tab, item, food, fuel, block, block item, model, loot, and tag helpers.
- `NexusData` recipes, advancements, translations, and configured feature JSON.
- `NexusConfig` typed option validation.
- `NexusCommands` and `NexusDebugCommands`.
- `DebugRegistry` live sections.
- `NexusNetworking` channel declaration.
- `RecipeViewerBridge` category/display registration consumed by JEI, EMI, and REI when installed.
- `EnergyStorage`, `FluidTank`, `QuickMoveRouter`, `CooldownTracker`, `ExpiringCache`, and `ValidationSuite`.

## Build

```powershell
.\gradlew.bat :example-fabric:build
.\gradlew.bat :example-neoforge:build
```

## Datagen

```powershell
.\gradlew.bat :example-fabric:runDatagen
.\gradlew.bat :example-neoforge:runDatagen
```
