package com.rollylindenshnizzer.nexuscore.api.registry;

import com.rollylindenshnizzer.nexuscore.core.service.NexusServices;

public final class NexusRegistries {
    public static final String ITEMS = "minecraft:item";
    public static final String BLOCKS = "minecraft:block";
    public static final String BLOCK_ITEMS = "nexuscore:block_item";
    public static final String CREATIVE_TABS = "minecraft:creative_mode_tab";
    public static final String COMMANDS = "minecraft:command";

    private NexusRegistries() {
    }

    public static <T> NexusEntry<T> register(String registryName, String id, NexusFactory<T> factory) {
        return NexusServices.get().registries().register(registryName, id, factory);
    }
}
