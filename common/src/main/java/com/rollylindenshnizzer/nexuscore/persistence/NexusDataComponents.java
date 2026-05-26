package com.rollylindenshnizzer.nexuscore.persistence;

import com.mojang.serialization.Codec;
import com.rollylindenshnizzer.nexuscore.core.NexusIds;
import com.rollylindenshnizzer.nexuscore.registry.NexusRegistries;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.component.DataComponentType;

public final class NexusDataComponents {
    public static <T> RegistrySupplier<DataComponentType<T>> persistent(String modId, String path, Codec<T> codec) {
        String normalized = NexusIds.normalizePath(path);
        return cast(NexusRegistries.group(modId).dataComponents()
                .register(normalized, () -> DataComponentType.<T>builder().persistent(codec).build()));
    }

    @SuppressWarnings("unchecked")
    private static <T> RegistrySupplier<DataComponentType<T>> cast(RegistrySupplier<DataComponentType<?>> supplier) {
        return (RegistrySupplier<DataComponentType<T>>) (RegistrySupplier<?>) supplier;
    }

    private NexusDataComponents() {
    }
}
