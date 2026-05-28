# v1.3 Cookbook

## Add a Dimension and Portal

```java
var dimension = NexusDimensions.register(NexusDimensions.dimension("yourmod", "crystal_caves")
        .singleBiome("minecraft:lush_caves")
        .build());

NexusDimensions.registerPortal(NexusDimensions.portal("yourmod", "crystal_portal")
        .targetDimension(dimension.id())
        .frame("minecraft:amethyst_block", 4, 5)
        .build());
```

## Add a Structure

```java
var structure = NexusStructures.register(NexusStructures.structure("yourmod", "watchtower")
        .template("yourmod:structures/watchtower.nbt")
        .placement(NexusStructures.StructurePlacementRule.common().spacing(28, 8).height(64, 160))
        .biome("#minecraft:is_overworld")
        .dimension("minecraft:overworld")
        .build());
```

## Add AI

```java
NexusAi.register(NexusAi.GoalLibrary.guardHome("yourmod", "guard_home",
        new NexusAi.HomePosition(new BlockPos(0, 70, 0), 16, true), 16));
```

## Add an Automation Network

```java
var network = NexusAutomation.network("yourmod", "starter")
        .node(NexusAutomation.TransferNode.item(new BlockPos(0, 64, 0), "source"))
        .node(NexusAutomation.TransferNode.item(new BlockPos(1, 64, 0), "target"))
        .connect(new BlockPos(0, 64, 0), new BlockPos(1, 64, 0), NexusAutomation.TransferKind.ITEM, 8)
        .build();
```

## Add a Data Definition Registry

```java
var registry = NexusDataDefinitions.registry("yourmod", "machine_profiles",
        new JsonSchema().require("type", JsonSchema.Type.STRING),
        json -> json.get("type").getAsString());
```

## Add a Balance Check

```java
var before = NexusBalance.report("before").metric("energy_per_tick", 10);
var after = NexusBalance.report("after").metric("energy_per_tick", 12);
var diff = NexusBalance.diff(before, after);
```

