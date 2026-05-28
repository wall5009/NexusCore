# NexusCore v1.2 Getting Started

v1.2 expands NexusCore from registration and tooling into gameplay systems: machines, inventory transfer, side-aware energy/fluid storage, generated machine screens, entity/worldgen/resource descriptors, client runtime descriptors, performance diagnostics, and scaffolding-backed examples.

Read this together with:

- `docs/system-catalog.md`
- `docs/scaffolding-walkthrough.md`
- `docs/v1.3/index.md`

## New Modules In v1.2

- `machine`: machine definitions, processing engine, machine recipes, redstone state, side configuration, upgrades, generated screen layouts.
- `inventory`: item handlers, slot roles/groups, transfer rules, transfer traces, snapshots, drop policies.
- `energy`: side-aware Nexus energy storage and transfer.
- `fluid`: side-aware Nexus fluid tanks and transfer.
- `ui.binding`: observable values and machine UI bindings.
- `client.machine`: owo machine screen runtime.
- `client`: descriptors and runtime hooks for keybinds, HUD layers, render layers, particles, sounds, renderers, and colors.
- `worldgen`: ore descriptors, biome selectors, configured/placed feature generation, Fabric runtime bridge, NeoForge biome modifiers.
- `entity`: entity descriptors, registration helpers, projectile descriptors, combat profiles.
- `resource`: schema-backed typed datapack loaders and data-driven registries.
- `security`: safe paths, server authority checks, client action guards.
- `performance`: benchmarks, profiler, caches, rate limiter, task queues, dirty tracking.

## Minimal v1.2 Machine

```java
NexusMachineDefinition crusher = NexusMachines.register(NexusMachines.machine(MOD_ID, "crusher")
        .category("processing")
        .energy(10_000, 250, 250)
        .fluid(4_000)
        .slots("input", SlotRole.INPUT, 0, 1)
        .slots("output", SlotRole.OUTPUT, 1, 2)
        .build());
```

Pair it with:

- `SimpleItemHandler` for inventory.
- `NexusEnergyStorage` for power.
- `NexusFluidTank` for fluids.
- `MachineState` for progress/status/redstone/side state.
- `MachineProcessingEngine` for ticking.
- `MachineScreenLayout.generated` for UI.

## Minimal v1.2 Resource Loader

```java
TypedDataLoader<RubyTrait> loader = new TypedDataLoader<>("ruby_traits",
        new JsonSchema().require("name", JsonSchema.Type.STRING).require("energy", JsonSchema.Type.NUMBER),
        json -> new RubyTrait(json.get("name").getAsString(), json.get("energy").getAsInt()));

DataDrivenRegistry<RubyTrait> registry = new DataDrivenRegistry<>(loader);
```

Generate matching JSON through `NexusData.plan(modId).data(...)`.

## Minimal v1.2 Worldgen

```java
NexusWorldgen.ore(MOD_ID, "ruby_ore")
        .state("example:ruby_ore")
        .veinSize(6)
        .count(8)
        .heightRange(-32, 64)
        .writeTo(NexusData.plan(MOD_ID));
```

## Minimal v1.2 Entity Descriptor

```java
NexusEntityDefinition definition = NexusEntityDefinitions.entity(MOD_ID, "ruby_marker", MobCategory.MISC)
        .sized(0.25F, 0.25F)
        .tracking(32, 10)
        .projectile(ProjectileDefinition.simple(2.0, 1.5F))
        .build();

RegisteredNexusEntity<AreaEffectCloud> registered =
        NexusEntityDefinitions.registerType(definition, AreaEffectCloud::new);
```

## Verification

Run:

```powershell
.\gradlew.bat build
.\gradlew.bat runNexusValidation
.\gradlew.bat :fabric:runGametest
.\gradlew.bat :neoforge:runGametest
```

For stable releases, also run binary compatibility checks and docs generation.
