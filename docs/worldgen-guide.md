# Worldgen Guide

NexusCore focuses on JSON generation and helper utilities for world interactions.

## Ore Features

```java
NexusData.plan(MOD_ID)
        .data("worldgen/configured_feature/ruby_ore.json", new OreFeatureJsonBuilder()
                .targetTag("minecraft:stone_ore_replaceables", MOD_ID + ":ruby_ore")
                .size(6)
                .discardChanceOnAirExposure(0.35F)
                .buildConfiguredFeature());
```

`OreFeatureJsonBuilder` creates configured feature JSON. `PlacedFeatureJsonBuilder` is available for placement JSON.

## Structure Helpers

`StructureHelpers` contains utility methods for structure-oriented code. Use it together with vanilla structure APIs when the mod owns the placement logic.

## World Helpers

The `world` package includes:

- `RaycastBuilder` for readable raycast setup.
- `TeleportTargetBuilder` for safe teleport target descriptors.
- `NexusWorlds` for common world lookups.

Keep generated worldgen JSON in data plans, and keep runtime world mutations in server-side code.
