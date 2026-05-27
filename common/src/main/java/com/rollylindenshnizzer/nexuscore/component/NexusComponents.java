package com.rollylindenshnizzer.nexuscore.component;

import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Map;

public final class NexusComponents {
    public static <T> NexusComponentBuilder<T> item(String modId, String path) {
        return new NexusComponentBuilder<>(modId, path);
    }

    public static Codec<Integer> colorCodec() {
        return Codec.INT.validate(value -> value >= 0x000000 && value <= 0xFFFFFF
                ? com.mojang.serialization.DataResult.success(value)
                : com.mojang.serialization.DataResult.error(() -> "Expected RGB color 0x000000..0xFFFFFF"));
    }

    public static Codec<Integer> rangeCodec(int min, int max) {
        return Codec.INT.validate(value -> value >= min && value <= max
                ? com.mojang.serialization.DataResult.success(value)
                : com.mojang.serialization.DataResult.error(() -> "Expected " + min + ".." + max));
    }

    public static <E extends Enum<E>> Codec<E> enumCodec(Class<E> enumClass) {
        return Codec.STRING.xmap(name -> Enum.valueOf(enumClass, name.toUpperCase(java.util.Locale.ROOT)),
                value -> value.name().toLowerCase(java.util.Locale.ROOT));
    }

    public static <T> Codec<List<T>> listCodec(Codec<T> elementCodec) {
        return elementCodec.listOf();
    }

    public static <T> Codec<Map<String, T>> mapCodec(Codec<T> valueCodec) {
        return Codec.unboundedMap(Codec.STRING, valueCodec);
    }

    public static <T> Codec<T> registryReferenceCodec(Registry<T> registry) {
        return ResourceLocation.CODEC.xmap(registry::get, registry::getKey);
    }

    private NexusComponents() {
    }
}
