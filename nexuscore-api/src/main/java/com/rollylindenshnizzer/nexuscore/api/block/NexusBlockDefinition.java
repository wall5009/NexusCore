package com.rollylindenshnizzer.nexuscore.api.block;

import java.util.LinkedHashMap;
import java.util.Map;

public final class NexusBlockDefinition {
    private final String modId;
    private final String id;
    private final float strength;
    private final float resistance;
    private final boolean requiresTool;
    private final boolean simpleItem;
    private final String creativeTab;
    private final Map<String, String> properties;

    public NexusBlockDefinition(String modId, String id, float strength, float resistance, boolean requiresTool, boolean simpleItem, String creativeTab, Map<String, String> properties) {
        this.modId = modId;
        this.id = id;
        this.strength = strength;
        this.resistance = resistance;
        this.requiresTool = requiresTool;
        this.simpleItem = simpleItem;
        this.creativeTab = creativeTab;
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

    public float strength() {
        return strength;
    }

    public float resistance() {
        return resistance;
    }

    public boolean requiresTool() {
        return requiresTool;
    }

    public boolean simpleItem() {
        return simpleItem;
    }

    public String creativeTab() {
        return creativeTab;
    }

    public Map<String, String> properties() {
        return new LinkedHashMap<>(properties);
    }
}
