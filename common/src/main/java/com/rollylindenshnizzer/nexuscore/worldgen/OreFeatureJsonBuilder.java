package com.rollylindenshnizzer.nexuscore.worldgen;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public final class OreFeatureJsonBuilder {
    private final JsonObject root = new JsonObject();
    private final JsonObject config = new JsonObject();
    private final JsonArray targets = new JsonArray();

    public OreFeatureJsonBuilder() {
        root.addProperty("type", "minecraft:ore");
        root.add("config", config);
        config.add("targets", targets);
    }

    public OreFeatureJsonBuilder targetTag(String replaceableTag, String stateName) {
        JsonObject target = new JsonObject();
        JsonObject targetPredicate = new JsonObject();
        targetPredicate.addProperty("predicate_type", "minecraft:tag_match");
        targetPredicate.addProperty("tag", replaceableTag);
        JsonObject state = new JsonObject();
        state.addProperty("Name", stateName);
        target.add("target", targetPredicate);
        target.add("state", state);
        targets.add(target);
        return this;
    }

    public OreFeatureJsonBuilder size(int size) {
        config.addProperty("size", size);
        return this;
    }

    public OreFeatureJsonBuilder discardChanceOnAirExposure(float chance) {
        config.addProperty("discard_chance_on_air_exposure", chance);
        return this;
    }

    public JsonObject buildConfiguredFeature() {
        return root.deepCopy();
    }
}
