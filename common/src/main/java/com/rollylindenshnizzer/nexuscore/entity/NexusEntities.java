package com.rollylindenshnizzer.nexuscore.entity;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

public final class NexusEntities {
    public static <T extends Entity> EntityTypeBuilder<T> entity(String modId, String path, EntityType.EntityFactory<T> factory, MobCategory category) {
        return EntityTypeBuilder.of(modId, path, factory, category);
    }

    private NexusEntities() {
    }
}
