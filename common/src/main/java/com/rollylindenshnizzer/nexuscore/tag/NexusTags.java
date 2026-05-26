package com.rollylindenshnizzer.nexuscore.tag;

import com.rollylindenshnizzer.nexuscore.core.NexusIds;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;

public final class NexusTags {
    public static TagKey<Item> item(String namespace, String path) {
        return tag(Registries.ITEM, namespace, path);
    }

    public static TagKey<Block> block(String namespace, String path) {
        return tag(Registries.BLOCK, namespace, path);
    }

    public static TagKey<EntityType<?>> entityType(String namespace, String path) {
        return tag(Registries.ENTITY_TYPE, namespace, path);
    }

    public static TagKey<Biome> biome(String namespace, String path) {
        return tag(Registries.BIOME, namespace, path);
    }

    public static TagKey<Fluid> fluid(String namespace, String path) {
        return tag(Registries.FLUID, namespace, path);
    }

    public static TagKey<DamageType> damageType(String namespace, String path) {
        return tag(Registries.DAMAGE_TYPE, namespace, path);
    }

    public static <T> TagKey<T> tag(ResourceKey<? extends Registry<T>> registry, String namespace, String path) {
        return TagKey.create(registry, NexusIds.id(namespace, path));
    }

    private NexusTags() {
    }
}
