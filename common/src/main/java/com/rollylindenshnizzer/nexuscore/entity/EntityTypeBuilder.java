package com.rollylindenshnizzer.nexuscore.entity;

import com.rollylindenshnizzer.nexuscore.core.NexusIds;
import com.rollylindenshnizzer.nexuscore.registry.NexusRegistries;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

@SuppressWarnings("unchecked")
public final class EntityTypeBuilder<T extends Entity> {
    private final String modId;
    private final String path;
    private final EntityType.Builder<T> builder;

    private EntityTypeBuilder(String modId, String path, EntityType.EntityFactory<T> factory, MobCategory category) {
        this.modId = NexusIds.requireNamespace(modId);
        this.path = NexusIds.normalizePath(path);
        this.builder = EntityType.Builder.of(factory, category);
    }

    public static <T extends Entity> EntityTypeBuilder<T> of(String modId, String path, EntityType.EntityFactory<T> factory, MobCategory category) {
        return new EntityTypeBuilder<>(modId, path, factory, category);
    }

    public EntityTypeBuilder<T> sized(float width, float height) {
        builder.sized(width, height);
        return this;
    }

    public EntityTypeBuilder<T> trackingRange(int range) {
        builder.clientTrackingRange(range);
        return this;
    }

    public EntityTypeBuilder<T> updateInterval(int ticks) {
        builder.updateInterval(ticks);
        return this;
    }

    public EntityTypeBuilder<T> fireImmune() {
        builder.fireImmune();
        return this;
    }

    public RegistrySupplier<EntityType<T>> register() {
        return (RegistrySupplier<EntityType<T>>) (RegistrySupplier<?>) NexusRegistries.group(modId).entityTypes()
                .register(path, () -> builder.build(modId + ":" + path));
    }
}
