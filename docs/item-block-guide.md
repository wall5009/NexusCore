# Item And Block Guide

The item and block builders cover the common content path: registry declaration, creative tab assignment, generated models, loot, tags, colors, tooltips, food, fuel, and simple interaction rules.

## Items

```java
RegistrySupplier<NexusItem> ruby = NexusItems.item(MOD_ID, "ruby")
        .creativeTab(tab)
        .tooltip("tooltip.example.ruby")
        .modelGenerated()
        .defaultColor(0xE43757)
        .register();
```

Useful item features:

- Generated item model through `modelGenerated`.
- Creative tab attachment.
- Tooltip translation key.
- Food via `NexusItems.food`.
- Fuel burn time.
- Default tint/color metadata.
- Custom use handler through `ItemInteractionBuilder`.

Food example:

```java
NexusItems.item(MOD_ID, "ruby_apple")
        .creativeTab(tab)
        .food(NexusItems.food().nutrition(6).saturation(0.8F).alwaysEdible().build())
        .modelGenerated()
        .register();
```

## Blocks

```java
RegistrySupplier<Block> rubyOre = NexusBlocks.block(MOD_ID, "ruby_ore")
        .strength(3.0F, 3.0F)
        .requiresCorrectTool()
        .mapColor(MapColor.STONE)
        .withBlockItem()
        .creativeTab(tab)
        .simpleCubeModel()
        .dropsSelf()
        .mineableWithPickaxe()
        .needsIronTool()
        .register();
```

Useful block features:

- Block item generation.
- Simple cube model and blockstate.
- Self-drop loot table.
- Mining tags.
- Required tool tags.
- Strength/resistance/map color.
- Custom factories for slabs, stairs, pillars, falling blocks, and specialized vanilla block classes.

## Block Sets

Use `NexusBlockSets` for material families:

```java
NexusBlockSet sapphireSet = NexusBlockSets.gem(MOD_ID, "sapphire")
        .material(MapColor.COLOR_BLUE)
        .strength(4.0F, 5.0F)
        .creativeTab(tab)
        .generateRecipes()
        .generateTags()
        .register();
```

Block sets can generate storage blocks, ore blocks, raw ore blocks, translations, recipes, tags, models, blockstates, and block items.

## Interaction Builders

Use interaction builders when behavior is rule-like:

```java
ItemInteractionBuilder builder = new ItemInteractionBuilder()
        .serverOnly(context -> {
            context.player().sendSystemMessage(Component.literal("Ruby used"));
            return InteractionResultHolder.success(context.stack());
        });
```

`BlockInteractionBuilder` provides similar predicate/result routing for use-on-block behavior.

## Loot, Tags, And Advancements

High-level builders generate common loot/tags. Use lower-level builders for custom data:

```java
plan.data("loot_table/tutorial/ruby_cache.json", LootTableBuilder.block()
        .selfDrop("example:ruby")
        .explosionSurvives()
        .build());

TagKey<Item> rubyGems = NexusTags.item(MOD_ID, "gems/ruby");
TagKey<Block> rubyOres = NexusTags.block(MOD_ID, "ores/ruby");
```

## Example

`NexusCoreExampleContent.beforeRegistries` demonstrates item, food, fuel, block, block item, model, loot, tag, color, and block set registration in one place.
