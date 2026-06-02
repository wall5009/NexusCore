package com.rollylindenshnizzer.nexuscore.api.item;

import java.util.LinkedHashMap;
import java.util.Map;

public final class NexusItemDefinition {
    private final String modId;
    private final String id;
    private final String creativeTab;
    private final int maxStackSize;
    private final boolean fireResistant;
    private final int foodNutrition;
    private final float foodSaturation;
    private final Map<String, String> properties;

    public NexusItemDefinition(String modId, String id, String creativeTab, int maxStackSize, boolean fireResistant, int foodNutrition, float foodSaturation, Map<String, String> properties) {
        this.modId = modId;
        this.id = id;
        this.creativeTab = creativeTab;
        this.maxStackSize = maxStackSize;
        this.fireResistant = fireResistant;
        this.foodNutrition = foodNutrition;
        this.foodSaturation = foodSaturation;
        this.properties = Map.copyOf(properties);
    }

    public String modId() {
        return modId;
    }

    public String id() {
        return id;
    }

    public String fullId() {
        return modId + ":" + id;
    }

    public String creativeTab() {
        return creativeTab;
    }

    public int maxStackSize() {
        return maxStackSize;
    }

    public boolean fireResistant() {
        return fireResistant;
    }

    public int foodNutrition() {
        return foodNutrition;
    }

    public float foodSaturation() {
        return foodSaturation;
    }

    public Map<String, String> properties() {
        return new LinkedHashMap<>(properties);
    }
}
