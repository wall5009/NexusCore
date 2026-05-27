package com.rollylindenshnizzer.nexuscore.block;

import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.world.level.block.Block;

import java.util.Map;

public record NexusBlockSet(String name, Map<String, RegistrySupplier<? extends Block>> blocks) {
    public RegistrySupplier<? extends Block> block(String part) {
        return blocks.get(part);
    }
}
