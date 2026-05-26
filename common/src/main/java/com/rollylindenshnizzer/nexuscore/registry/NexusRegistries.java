package com.rollylindenshnizzer.nexuscore.registry;

import com.rollylindenshnizzer.nexuscore.core.NexusIds;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public final class NexusRegistries {
    private static final Map<String, NexusRegistryGroup> GROUPS = new ConcurrentHashMap<>();

    public static NexusRegistryGroup group(String modId) {
        return GROUPS.computeIfAbsent(NexusIds.requireNamespace(modId), NexusRegistryGroup::new);
    }

    public static <I extends Item> RegistrySupplier<I> item(String modId, String path, Supplier<I> supplier) {
        NexusRegistryGroup group = group(modId);
        return group.items().register(path, supplier);
    }

    public static <B extends Block> RegistrySupplier<B> block(String modId, String path, Supplier<B> supplier) {
        NexusRegistryGroup group = group(modId);
        return group.blocks().register(path, supplier);
    }

    public static <T> Optional<T> optional(Registry<T> registry, ResourceLocation id) {
        return registry.containsKey(id) ? Optional.of(registry.get(id)) : Optional.empty();
    }

    public static <T> T require(Registry<T> registry, ResourceLocation id, String reason) {
        return optional(registry, id).orElseThrow(() -> new MissingRegistryEntryException(id, reason));
    }

    public static <T> ResourceKey<T> key(ResourceKey<? extends Registry<T>> registry, String modId, String path) {
        return ResourceKey.create(registry, NexusIds.id(modId, path));
    }

    public static void registerAll(String modId) {
        group(modId).registerAll();
    }

    private NexusRegistries() {
    }
}
