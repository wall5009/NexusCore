package com.rollylindenshnizzer.nexuscore.worldgen;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public final class WorldgenJson {
    public static JsonObject uniformHeight(int minInclusive, int maxInclusive) {
        JsonObject height = new JsonObject();
        height.addProperty("type", "minecraft:uniform");
        JsonObject min = new JsonObject();
        min.addProperty("absolute", minInclusive);
        JsonObject max = new JsonObject();
        max.addProperty("absolute", maxInclusive);
        height.add("min_inclusive", min);
        height.add("max_inclusive", max);
        return height;
    }

    public static JsonObject countPlacement(int count) {
        JsonObject placement = new JsonObject();
        placement.addProperty("type", "minecraft:count");
        placement.addProperty("count", count);
        return placement;
    }

    public static JsonObject biomePlacement() {
        JsonObject placement = new JsonObject();
        placement.addProperty("type", "minecraft:biome");
        return placement;
    }

    public static JsonArray placements(JsonObject... placements) {
        JsonArray array = new JsonArray();
        for (JsonObject placement : placements) {
            array.add(placement);
        }
        return array;
    }

    private WorldgenJson() {
    }
}
