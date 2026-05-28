# Worldgen Guide

NexusCore v1.2 provides ore generation descriptors, JSON builders, validation reports, and loader bridges.

## Ore Descriptor

```java
OreGenerationBuilder rubyOre = NexusWorldgen.ore(MOD_ID, "ruby_ore")
        .state("example:ruby_ore")
        .veinSize(6)
        .count(8)
        .heightRange(-32, 64)
        .biomes(BiomeSelector.include("#minecraft:is_overworld"));
```

The descriptor stores:

- Mod ID and path.
- Block state ID.
- Vein size.
- Count per chunk.
- Height range.
- Biome selector includes/excludes.

## Datagen

Write the descriptor to a data plan:

```java
rubyOre.writeTo(NexusData.plan(MOD_ID));
```

Generated output includes:

- `worldgen/configured_feature/<path>.json`
- `worldgen/placed_feature/<path>.json`
- NeoForge `neoforge/biome_modifier/<path>.json`

Fabric applies registered ores at runtime through the Fabric worldgen bridge.

## Biome Selectors

`BiomeSelector` supports include and exclude lists. Values may be concrete biome IDs or tags, for example:

```java
BiomeSelector selector = BiomeSelector.include("#minecraft:is_overworld")
        .exclude("minecraft:deep_dark");
```

Keep selectors explicit. If a feature must only appear in a small biome set, list those biomes instead of broadly including the overworld and excluding many cases.

## Lower-Level JSON Builders

Use `OreFeatureJsonBuilder`, `PlacedFeatureJsonBuilder`, and `WorldgenJson` for custom output:

```java
plan.data("worldgen/configured_feature/ruby_ore.json", new OreFeatureJsonBuilder()
        .targetTag("minecraft:stone_ore_replaceables", "example:ruby_ore")
        .size(6)
        .discardChanceOnAirExposure(0.35F)
        .buildConfiguredFeature());
```

## Validation

Use `WorldgenValidationReport` to report missing states, invalid ranges, empty selectors, or incompatible generation settings. Also run `NexusDataValidator` on the generated plan.

## World Helpers

The `world` package complements worldgen:

- `NexusWorlds.positions` and `radius` for safe block iteration.
- `NexusWorlds.nearbyEntities` for bounded entity queries.
- `NexusWorlds.entity` for UUID lookup.
- `RaycastBuilder` for configurable block/fluid raycasts.
- `TeleportTargetBuilder` for dimension/position/rotation targets.
- `StructureHelpers` for bounding box conversion and centers.

## Example

The scaffolded coverage ruby ore descriptor is registered in `scaffolded registry code`. `NexusCoreGameTestScenarios` exercises world radius iteration, structure centers, and teleport target descriptors.
