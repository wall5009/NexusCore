# Datagen Guide

`NexusData` is a lightweight data plan for assets and data JSON. Builders write into the plan as content is declared, and loader-specific providers flush the plan during datagen.

## Create A Plan

```java
NexusData.plan(MOD_ID)
        .translation("item.example.ruby", "Ruby")
        .itemGenerated("ruby")
        .blockCubeAll("ruby_block")
        .lootDropsSelf("ruby_block");
```

Plans are keyed by mod ID and can be populated from content builders or explicit datagen code.

## Add Recipes

```java
NexusData.plan(MOD_ID)
        .data("recipe/ruby_block.json", RecipeJsonBuilder.shaped("building", MOD_ID + ":ruby_block", 1)
                .pattern("RRR", "RRR", "RRR")
                .key('R', MOD_ID + ":ruby")
                .build());
```

`RecipeJsonBuilder` supports shaped, shapeless, and cooking recipes.

## Add Advancements

```java
NexusData.plan(MOD_ID)
        .data("advancement/root.json", new AdvancementJsonBuilder()
                .display(MOD_ID + ":ruby", "advancements.example.root.title",
                        "advancements.example.root.description", "task", true, false, false)
                .criterion("has_ruby", AdvancementJsonBuilder.inventoryChanged(MOD_ID + ":ruby"))
                .build());
```

## Provider Wiring

Fabric uses `NexusDataProvider` from a `DataGeneratorEntrypoint`.

NeoForge uses the same provider from `GatherDataEvent`.

Both loader modules for NexusCore are wired already. The example mod shows the pattern for a consuming mod in `examples/example-mod/fabric` and `examples/example-mod/neoforge`.

## Validation

`NexusDataValidator` checks plans before writing. `DatagenBootstrap.strict()` turns validation errors into failures, which is useful in CI.
