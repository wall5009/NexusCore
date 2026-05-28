# Datagen Guide

`NexusData` lets common code build generated assets and data once, then write them from Fabric and NeoForge datagen entrypoints.

## Data Plans

Create a plan per mod ID:

```java
NexusData.DataPlan plan = NexusData.plan(MOD_ID)
        .translation("item.example.ruby", "Ruby")
        .itemGenerated("ruby")
        .blockCubeAll("ruby_ore")
        .lootDropsSelf("ruby_ore")
        .tag("items", "gems/ruby", "example:ruby");
```

Plans contain:

- `translations`: written to `assets/<modid>/lang/en_us.json`.
- `assets`: written under `assets/<modid>/`.
- `data`: written under `data/<modid>/`.

Every generated entry is recorded in `NexusContentManifest`.

## Recipes

Use `RecipeJsonBuilder` for common recipe shapes:

```java
plan.data("recipe/ruby_block.json", RecipeJsonBuilder.shaped("building", "example:ruby_block", 1)
        .pattern("RRR", "RRR", "RRR")
        .key('R', "example:ruby")
        .build());

plan.data("recipe/ruby_from_block.json", RecipeJsonBuilder.shapeless("misc", "example:ruby", 9)
        .ingredient("example:ruby_block")
        .build());

plan.data("recipe/ruby_from_smelting.json", RecipeJsonBuilder.cooking("minecraft:smelting", "misc",
        "example:raw_ruby", "example:ruby", 0.7F, 200).build());
```

Run `NexusDataValidator.validatePlan(plan)` to catch missing result fields, empty tags, invalid blockstates, duplicate paths, and blank translations.

## Advancements

```java
plan.data("advancement/root.json", new AdvancementJsonBuilder()
        .display("example:ruby", "advancements.example.root.title",
                "advancements.example.root.description", "task", true, false, false)
        .criterion("has_ruby", AdvancementJsonBuilder.inventoryChanged("example:ruby"))
        .build());
```

Keep translation keys in the plan so the advancement remains localizable.

## Loot And Tags

High-level block builders can generate self-drop loot and mining tags. Use lower-level helpers for custom files:

```java
plan.data("loot_table/tutorial/ruby_cache.json", LootTableBuilder.block()
        .selfDrop("example:ruby")
        .explosionSurvives()
        .build());

plan.tag("items", "gems/ruby", "example:ruby");
plan.tag("blocks", "ores/ruby", "example:ruby_ore");
```

## Worldgen

v1.2 ore descriptors can write configured features, placed features, and loader-specific biome modifiers:

```java
NexusWorldgen.ore(MOD_ID, "ruby_ore")
        .state("example:ruby_ore")
        .veinSize(6)
        .count(8)
        .heightRange(-32, 64)
        .writeTo(plan);
```

NeoForge consumes generated biome modifier JSON. Fabric also has a runtime bridge that applies descriptors through Fabric Biome API.

## Typed Datapack JSON

For custom datapack folders, pair generated JSON with a `TypedDataLoader`:

```java
JsonObject json = new JsonObject();
json.addProperty("name", "polished_ruby");
json.addProperty("energy", 120);
plan.data("ruby_traits/polished_ruby.json", json);
```

Runtime loading:

```java
TypedDataLoader<RubyTrait> loader = new TypedDataLoader<>("ruby_traits",
        new JsonSchema().require("name", JsonSchema.Type.STRING).require("energy", JsonSchema.Type.NUMBER),
        object -> new RubyTrait(object.get("name").getAsString(), object.get("energy").getAsInt()));
```

## Providers

Fabric:

```java
pack.addProvider((FabricDataGenerator.Pack.Factory<NexusDataProvider>) output ->
        new NexusDataProvider(output, MOD_ID, populateGeneratedData()));
```

NeoForge:

```java
event.addProvider(new NexusDataProvider(event.getGenerator().getPackOutput(), MOD_ID, populateGeneratedData()));
```

## Reports

Use report writers in CI or release tasks:

```java
DataValidationReport report = NexusDataValidator.validatePlan(plan);
DatagenReportWriters.writeMarkdown(report, output.resolve("datagen.md"));
DatagenReportWriters.writeJson(report, output.resolve("datagen.json"));
DatagenReportWriters.writeHtml(report, output.resolve("datagen.html"));
```

## Example

Use `docs/scaffolding-walkthrough.md` to generate focused datagen starter classes. Inspect the generated resources under each loader module's configured datagen output after running `:fabric:runDatagen` or `:neoforge:runDatagen`.
