# Dimensions, Structures, and Biomes

## Dimensions

Create a dimension descriptor:

```java
var moon = NexusDimensions.register(NexusDimensions.dimension("yourmod", "moon")
        .singleBiome("minecraft:plains")
        .noiseGenerator("minecraft:overworld")
        .config(new NexusDimensions.DimensionConfigProfile(false, false, 1.0D, 20000, 18000))
        .build());
```

The descriptor can write dimension JSON, dimension type JSON, language keys, and NexusCore metadata into a `NexusData.DataPlan`. `NexusDimensions.validate` checks duplicate IDs, missing biome/type references at descriptor level, invalid coordinate scales, unsafe platform radius, portal target references, and cooldown values.

Portals are data descriptors:

```java
NexusDimensions.registerPortal(NexusDimensions.portal("yourmod", "moon_portal")
        .targetDimension(moon.id())
        .frame("minecraft:obsidian", 4, 5)
        .cooldownTicks(80)
        .build());
```

`PortalFramePattern.matches` gives tests and debug tools a deterministic frame detector. `NexusRuntimeContent.install(modId)` registers concrete portal blocks for portal descriptors, generates the block/item assets, and exposes dimension and portal metadata through the runtime data pack.

## Structures

`NexusStructures.structure` stores template metadata, loot table references, entity markers, required blocks, processors, biome selectors, dimension selectors, spacing, separation, rarity, and terrain constraints. It writes structure and structure set JSON and returns placement explanations for debug commands.

Jigsaw authoring is stable in v1.3. `JigsawPoolDefinition` writes vanilla template-pool JSON into the runtime pack, participates in structure validation, and is available to the same debug/reporting path as normal structure descriptors. The older `JigsawPoolPreview` name remains as a source-compatible alias for existing callers.

## Biomes

`NexusBiomes.biome` builds biome JSON with climate, environment effects, features, spawns, and balance weights. `BiomeSelector` supports tag, dimension, and climate expressions. `BiomeModifierGroup` groups selectors with feature and spawn additions for datagen/reporting workflows.
