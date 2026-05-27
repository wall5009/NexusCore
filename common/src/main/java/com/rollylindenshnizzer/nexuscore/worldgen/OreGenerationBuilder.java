package com.rollylindenshnizzer.nexuscore.worldgen;

import com.google.gson.JsonObject;
import com.rollylindenshnizzer.nexuscore.api.NexusStable;
import com.rollylindenshnizzer.nexuscore.data.NexusData;

@NexusStable(since = "1.2")
public final class OreGenerationBuilder {
    private final String modId;
    private final String path;
    private String targetTag = "minecraft:stone_ore_replaceables";
    private String stateName;
    private int veinSize = 6;
    private float discardChance = 0.0F;
    private int count = 8;
    private int minY = -64;
    private int maxY = 64;
    private BiomeSelector biomes = BiomeSelector.all();

    OreGenerationBuilder(String modId, String path) {
        this.modId = modId;
        this.path = path;
        this.stateName = modId + ":" + path;
    }

    public OreGenerationBuilder targetTag(String targetTag) {
        this.targetTag = targetTag;
        return this;
    }

    public OreGenerationBuilder state(String stateName) {
        this.stateName = stateName;
        return this;
    }

    public OreGenerationBuilder veinSize(int veinSize) {
        this.veinSize = Math.max(1, veinSize);
        return this;
    }

    public OreGenerationBuilder discardChanceOnAirExposure(float chance) {
        this.discardChance = Math.max(0.0F, Math.min(1.0F, chance));
        return this;
    }

    public OreGenerationBuilder count(int count) {
        this.count = Math.max(1, count);
        return this;
    }

    public OreGenerationBuilder heightRange(int minY, int maxY) {
        this.minY = minY;
        this.maxY = Math.max(minY, maxY);
        return this;
    }

    public OreGenerationBuilder biomes(BiomeSelector biomes) {
        this.biomes = biomes == null ? BiomeSelector.all() : biomes;
        return this;
    }

    public JsonObject configuredFeature() {
        return new OreFeatureJsonBuilder()
                .targetTag(targetTag, stateName)
                .size(veinSize)
                .discardChanceOnAirExposure(discardChance)
                .buildConfiguredFeature();
    }

    public JsonObject placedFeature() {
        PlacedFeatureJsonBuilder builder = new PlacedFeatureJsonBuilder(modId + ":" + path);
        JsonObject countPlacement = new JsonObject();
        countPlacement.addProperty("type", "minecraft:count");
        countPlacement.addProperty("count", count);
        builder.placement(countPlacement);

        JsonObject inSquare = new JsonObject();
        inSquare.addProperty("type", "minecraft:in_square");
        builder.placement(inSquare);

        JsonObject heightRange = new JsonObject();
        heightRange.addProperty("type", "minecraft:height_range");
        JsonObject height = new JsonObject();
        height.addProperty("type", "minecraft:uniform");
        JsonObject min = new JsonObject();
        min.addProperty("absolute", minY);
        JsonObject max = new JsonObject();
        max.addProperty("absolute", maxY);
        height.add("min_inclusive", min);
        height.add("max_inclusive", max);
        heightRange.add("height", height);
        builder.placement(heightRange);

        JsonObject biome = new JsonObject();
        biome.addProperty("type", "minecraft:biome");
        builder.placement(biome);
        return builder.build();
    }

    public JsonObject biomeModifier() {
        JsonObject json = new JsonObject();
        json.addProperty("type", "neoforge:add_features");
        json.add("biomes", biomes.toBiomeModifierPredicate());
        json.addProperty("features", modId + ":" + path);
        json.addProperty("step", "underground_ores");
        return json;
    }

    public NexusData.DataPlan writeTo(NexusData.DataPlan plan) {
        return plan.data("worldgen/configured_feature/" + path + ".json", configuredFeature())
                .data("worldgen/placed_feature/" + path + ".json", placedFeature())
                .data("neoforge/biome_modifier/" + path + ".json", biomeModifier());
    }

    public String modId() {
        return modId;
    }

    public String path() {
        return path;
    }

    public BiomeSelector biomes() {
        return biomes;
    }
}
