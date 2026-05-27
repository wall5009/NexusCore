package com.rollylindenshnizzer.nexuscore.entity;

import com.rollylindenshnizzer.nexuscore.api.NexusStable;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;

import java.util.Optional;

@NexusStable(since = "1.2")
public record RegisteredNexusEntity<T extends Entity>(NexusEntityDefinition definition,
                                                      RegistrySupplier<EntityType<T>> type,
                                                      Optional<RegistrySupplier<Item>> spawnEgg) {
    public RegisteredNexusEntity {
        spawnEgg = spawnEgg == null ? Optional.empty() : spawnEgg;
    }
}
