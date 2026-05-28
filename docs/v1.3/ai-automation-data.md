# AI, Automation, and Data Definitions

## AI

`NexusAi.goal` creates stable goal descriptors with priority, type, controls, parameters, home, territory, patrol route, and schedule. `NexusAi.GoalLibrary` includes common guarded-home, follow-owner, and patrol goals.

Helpers:

- `HomePosition`: persistent home center and radius.
- `Territory`: defendable radius with containment checks.
- `PatrolRoute`: looped or clamped point lists.
- `PathCache`: reusable path storage for AI systems.
- `StuckDetector`: repeated-position detector for GameTests and runtime debugging.
- `BrainDefinition`: stable memory, sensor, behavior, and schedule descriptor for data-driven AI authoring.

## Automation

`NexusAutomation.network` creates a routing graph of `TransferNode` and `TransferEdge` values. Edges declare item, energy, fluid, or any-kind transfer, throughput, priority, and item filters. `route` performs deterministic path planning; `simulate` reports total throughput and loops.

Use `AutomationBlockPreset.itemPipe`, `energyConduit`, and `fluidPipe` for starter tuning values. `NexusRuntimeContent.install(modId)` registers concrete pipe/conduit blocks, block items, blockstate/model data, loot tables, and translations before the owning registry group is frozen.

## Data Definitions

`NexusDataDefinitions.registry` creates a typed JSON registry with schema validation, safety checks, reload reporting, docs generation, and decoded values.

Unsafe JSON checks block `unsafe_command`, warn on `client_script`, and warn on very large payloads. `DataDrivenEntityDefinition` registers a real generic entity type through `NexusEntityDefinitions`, and `DataDrivenWorldgenDefinition` writes dynamic-registry JSON into the generated runtime pack.

## Runtime Pack

The v1.3 runtime installer materializes data plans into an always-enabled in-memory pack. Fabric receives it through the NexusCore pack repository mixin, and NeoForge receives it through `AddPackFindersEvent`. This is the bridge that makes dimension, structure, jigsaw, biome, worldgen, data-driven entity, and generated asset JSON visible to vanilla's dynamic registry/resource loading path at runtime, not only during datagen.
