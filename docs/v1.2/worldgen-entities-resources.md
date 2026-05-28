# Worldgen, Entities, And Resources In v1.2

v1.2 adds descriptor-driven worldgen, entity registration helpers, and schema-backed datapack loaders.

## Worldgen

```java
NexusWorldgen.ore(MOD_ID, "ruby_ore")
        .state("example:ruby_ore")
        .veinSize(6)
        .count(8)
        .heightRange(-32, 64)
        .biomes(BiomeSelector.include("#minecraft:is_overworld"))
        .writeTo(NexusData.plan(MOD_ID));
```

Generated output:

- Configured feature JSON.
- Placed feature JSON.
- NeoForge biome modifier JSON.

Runtime output:

- Fabric bridge installs ore descriptors through Fabric Biome API.
- NeoForge consumes generated biome modifiers.

## Entity Descriptors

```java
NexusEntityDefinition definition = NexusEntityDefinitions.entity(MOD_ID, "ruby_marker", MobCategory.MISC)
        .sized(0.25F, 0.25F)
        .tracking(32, 10)
        .attribute("lifetime_ticks", 80.0)
        .projectile(ProjectileDefinition.simple(2.0, 1.5F))
        .build();
```

Register a type:

```java
RegisteredNexusEntity<AreaEffectCloud> marker =
        NexusEntityDefinitions.registerType(definition, AreaEffectCloud::new);
```

For mobs, use `registerMobType` to also register spawn eggs when the descriptor requests one.

## Projectile And Combat Metadata

`ProjectileDefinition` describes damage, speed, divergence, gravity, pierce level, and pickup behavior.

`CombatProfile` describes attack damage, attack speed, armor, toughness, knockback resistance, and equipment loot tables. Use it in docs/debug output or to drive entity setup code.

## Typed Resources

```java
TypedDataLoader<RubyTrait> loader = new TypedDataLoader<>("ruby_traits",
        new JsonSchema()
                .require("name", JsonSchema.Type.STRING)
                .require("energy", JsonSchema.Type.NUMBER)
                .optional("requires_water", JsonSchema.Type.BOOLEAN),
        json -> new RubyTrait(json.get("name").getAsString(),
                json.get("energy").getAsInt(),
                json.has("requires_water") && json.get("requires_water").getAsBoolean()));

DataDrivenRegistry<RubyTrait> registry = new DataDrivenRegistry<>(loader);
ResourceValidationReport report = registry.reload(jsonObjects);
```

Generated matching JSON:

```java
JsonObject json = new JsonObject();
json.addProperty("name", "polished_ruby");
json.addProperty("energy", 120);
json.addProperty("requires_water", true);
plan.data("ruby_traits/polished_ruby.json", json);
```

## Example

The scaffolding and GameTest coverage registers ruby ore worldgen, a ruby marker entity, projectile metadata, combat profile metadata, and a typed `ruby_traits` datapack loader.
