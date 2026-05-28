# Migration From v1.1 To v1.2

v1.2 is additive and keeps v1.0/v1.1 binary compatibility. Existing v1.1 mods should continue to compile, but several helper-level systems now have richer runtime APIs.

## Recommended Migration Steps

1. Keep existing item, block, config, datagen, networking, and recipe viewer code working.
2. Move ad hoc machine state into `NexusMachineDefinition`, `MachineRecipeDefinition`, `MachineState`, `SimpleItemHandler`, `NexusEnergyStorage`, and `NexusFluidTank`.
3. Replace hand-written shift-click logic with `QuickMoveRouter` or `TransferRule` plus `InventoryTransfer`.
4. Replace custom energy/fluid bridge code with NexusCore storage plus loader bridges.
5. Move custom datapack JSON parsing into `TypedDataLoader` and `DataDrivenRegistry`.
6. Represent worldgen through `NexusWorldgen.ore` where the ore helper fits.
7. Represent entity metadata through `NexusEntityDefinition`; use `registerType` or `registerMobType` when NexusCore can register the type.
8. Add debug sections and `NexusDoctor` checks for every migrated subsystem.
9. Add validation suite checks or GameTests for machine processing, transfer, and datapack loading.

## API Compatibility

Stable APIs remain binary compatible. Do not remove public methods or change public signatures in downstream extensions without a major-version plan.

Run:

```powershell
.\gradlew.bat checkBinaryCompatibility -PnexusApiBaseline=common/build/libs/nexuscore-common-1.0.0-build.3.jar -PnexusRelease=true
```

## Recipe Viewers

v1.1 introduced concrete JEI/EMI/REI plugins and advanced recipe layout descriptors. v1.2 keeps those APIs and the scaffolded coverage now demonstrates multi-page displays, fluids, progress, and viewer-specific controls.

## Config Screens

The v1.1 owo config bridge is now a full generated config editor screen. Keep config definitions in common code and open screens through `NexusConfigScreens` or `OwoConfigBridge`.

## Client Runtime

If v1.1 code only registered descriptors, v1.2 can now install real runtime client effects. Keep descriptors common-safe and put actual callbacks/renderers in client paths.

## Generated Data

`NexusDataProvider` now also writes registered worldgen and entity descriptor data for the target mod ID. Review generated output after migration.

## Examples

Use `docs/scaffolding-walkthrough.md` and `docs/v1.3/cookbook.md` as the maintained migration targets. The embedded example mod was removed in v1.3, but the v1.2 public APIs remain covered by `checkV12SourceCompatibility` and GameTest scenarios.
