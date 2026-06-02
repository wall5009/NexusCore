package com.rollylindenshnizzer.nexuscore.api.item;

import com.rollylindenshnizzer.nexuscore.api.NexusMod;
import com.rollylindenshnizzer.nexuscore.api.registry.NexusEntry;
import com.rollylindenshnizzer.nexuscore.api.registry.NexusRegistries;
import com.rollylindenshnizzer.nexuscore.core.service.NexusServices;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public final class NexusItems {
    private NexusItems() {
    }

    public static Builder create(NexusMod mod, String id) {
        return new Builder(mod.id(), id);
    }

    public static final class Builder {
        private final String modId;
        private final String id;
        private String creativeTab = "";
        private int maxStackSize = 64;
        private boolean fireResistant;
        private int foodNutrition;
        private float foodSaturation;
        private final Map<String, String> properties = new LinkedHashMap<>();

        private Builder(String modId, String id) {
            this.modId = Objects.requireNonNull(modId, "modId");
            this.id = Objects.requireNonNull(id, "id");
        }

        public Builder creativeTab(String creativeTab) {
            this.creativeTab = Objects.requireNonNull(creativeTab, "creativeTab");
            return this;
        }

        public Builder maxStackSize(int maxStackSize) {
            if (maxStackSize < 1 || maxStackSize > 64) {
                throw new IllegalArgumentException("NexusCore item '" + modId + ":" + id + "' has invalid max stack size " + maxStackSize + ". Use 1 through 64.");
            }
            this.maxStackSize = maxStackSize;
            return this;
        }

        public Builder fireResistant() {
            this.fireResistant = true;
            return this;
        }

        public Builder food(int nutrition, float saturation) {
            if (nutrition < 0) {
                throw new IllegalArgumentException("NexusCore item '" + modId + ":" + id + "' has invalid food nutrition " + nutrition + ".");
            }
            this.foodNutrition = nutrition;
            this.foodSaturation = saturation;
            return this;
        }

        public Builder property(String key, String value) {
            this.properties.put(Objects.requireNonNull(key, "key"), Objects.requireNonNull(value, "value"));
            return this;
        }

        public NexusEntry<NexusItemHandle> register() {
            NexusItemDefinition definition = new NexusItemDefinition(modId, id, creativeTab, maxStackSize, fireResistant, foodNutrition, foodSaturation, properties);
            return NexusRegistries.register(NexusRegistries.ITEMS, definition.fullId(), ignored -> {
                Object nativeItem = NexusServices.get().itemFactory().createItem(definition);
                return new NexusItemHandle(definition, nativeItem);
            });
        }
    }
}
