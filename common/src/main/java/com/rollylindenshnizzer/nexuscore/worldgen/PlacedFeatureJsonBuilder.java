package com.rollylindenshnizzer.nexuscore.worldgen;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public final class PlacedFeatureJsonBuilder {
    private final JsonObject root = new JsonObject();
    private final JsonArray placement = new JsonArray();

    public PlacedFeatureJsonBuilder(String configuredFeature) {
        root.addProperty("feature", configuredFeature);
        root.add("placement", placement);
    }

    public PlacedFeatureJsonBuilder placement(JsonObject modifier) {
        placement.add(modifier);
        return this;
    }

    public JsonObject build() {
        return root.deepCopy();
    }
}
