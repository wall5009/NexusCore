# Item And Block Guide

The item and block builders focus on common modding defaults: registration, creative tabs, generated assets, loot tables, tags, fuel, food, map color, and block items.

## Items

```java
RegistrySupplier<NexusItem> ruby = NexusItems.item(MOD_ID, "ruby")
        .creativeTab(tab)
        .tooltip("tooltip.example.ruby")
        .modelGenerated()
        .defaultColor(0xE43757)
        .register();
```

Useful item helpers:

- `modelGenerated()` adds an item model to the datagen plan.
- `tooltip(key)` adds a translated tooltip.
- `fuel(ticks)` registers fuel time.
- `food(NexusItems.food().nutrition(...).build())` builds food properties.
- `defaultColor(color)` records a default color for diagnostics or generated tooling.

## Blocks

```java
RegistrySupplier<Block> rubyBlock = NexusBlocks.block(MOD_ID, "ruby_block")
        .strength(5.0F, 6.0F)
        .requiresCorrectTool()
        .mapColor(MapColor.COLOR_RED)
        .withBlockItem()
        .creativeTab(tab)
        .simpleCubeModel()
        .dropsSelf()
        .mineableWithPickaxe()
        .needsIronTool()
        .register();
```

Useful block helpers:

- `withBlockItem()` registers a matching item.
- `simpleCubeModel()` adds block model, item model, and blockstate JSON to the data plan.
- `dropsSelf()` adds a self-drop loot table.
- `mineableWithPickaxe()` and `needsIronTool()` add tag data.
- `mapColor`, `strength`, and `requiresCorrectTool` mirror vanilla block properties.

## Interactions

`ItemInteractionBuilder` and `BlockInteractionBuilder` help compose right-click behavior without burying checks in one large method. Use them for simple contextual behavior; custom subclasses are still appropriate for complex blocks.
