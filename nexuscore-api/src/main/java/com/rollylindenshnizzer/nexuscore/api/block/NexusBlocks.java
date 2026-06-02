package com.rollylindenshnizzer.nexuscore.api.block;

import com.rollylindenshnizzer.nexuscore.api.NexusMod;
import com.rollylindenshnizzer.nexuscore.api.registry.NexusEntry;
import com.rollylindenshnizzer.nexuscore.api.registry.NexusRegistries;
import com.rollylindenshnizzer.nexuscore.core.service.NexusServices;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public final class NexusBlocks {
    private NexusBlocks() {
    }

    public static Builder create(NexusMod mod, String id) {
        return new Builder(mod.id(), id);
    }

    public static final class Builder {
        private final String modId;
        private final String id;
        private float strength = 1.0f;
        private float resistance = 1.0f;
        private boolean requiresTool;
        private boolean simpleItem;
        private String creativeTab = "building_blocks";
        private final Map<String, String> properties = new LinkedHashMap<>();

        private Builder(String modId, String id) {
            this.modId = Objects.requireNonNull(modId, "modId");
            this.id = Objects.requireNonNull(id, "id");
        }

        public Builder strength(float strength) {
            if (strength < 0.0f) {
                throw new IllegalArgumentException("NexusCore block '" + modId + ":" + id + "' has negative strength " + strength + ".");
            }
            this.strength = strength;
            this.resistance = strength;
            return this;
        }

        public Builder resistance(float resistance) {
            if (resistance < 0.0f) {
                throw new IllegalArgumentException("NexusCore block '" + modId + ":" + id + "' has negative resistance " + resistance + ".");
            }
            this.resistance = resistance;
            return this;
        }

        public Builder requiresTool() {
            this.requiresTool = true;
            return this;
        }

        public Builder withSimpleItem() {
            this.simpleItem = true;
            return this;
        }

        public Builder creativeTab(String creativeTab) {
            this.creativeTab = Objects.requireNonNull(creativeTab, "creativeTab");
            return this;
        }

        public Builder property(String key, String value) {
            this.properties.put(Objects.requireNonNull(key, "key"), Objects.requireNonNull(value, "value"));
            return this;
        }

        public NexusEntry<NexusBlockHandle> register() {
            NexusBlockDefinition definition = new NexusBlockDefinition(modId, id, strength, resistance, requiresTool, simpleItem, creativeTab, properties);
            Object nativeBlock = NexusServices.get().blockFactory().createBlock(definition);
            Object nativeBlockItem = simpleItem ? NexusServices.get().blockFactory().createBlockItem(definition, nativeBlock) : null;
            NexusEntry<NexusBlockHandle> block = NexusRegistries.register(NexusRegistries.BLOCKS, definition.fullId(), ignored -> new NexusBlockHandle(definition, nativeBlock, nativeBlockItem));
            if (simpleItem) {
                NexusRegistries.register(NexusRegistries.BLOCK_ITEMS, definition.fullId(), ignored -> new NexusBlockHandle(definition, nativeBlock, nativeBlockItem));
            }
            return block;
        }
    }
}
