package com.rollylindenshnizzer.nexuscore.persistence;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.resources.ResourceLocation;

import java.util.Arrays;
import java.util.Locale;
import java.util.function.Function;

public final class NexusCodecs {
    public static final Codec<ResourceLocation> IDENTIFIER = ResourceLocation.CODEC;

    public static <E extends Enum<E>> Codec<E> enumCodec(Class<E> enumClass) {
        return Codec.STRING.comapFlatMap(name -> {
            for (E value : enumClass.getEnumConstants()) {
                if (value.name().equalsIgnoreCase(name)) {
                    return DataResult.success(value);
                }
            }
            return DataResult.error(() -> "Expected one of " + Arrays.toString(enumClass.getEnumConstants()));
        }, value -> value.name().toLowerCase(Locale.ROOT));
    }

    public static <T> Codec<T> boundedInt(int min, int max, Function<Integer, T> reader, Function<T, Integer> writer) {
        return Codec.INT.comapFlatMap(value -> value >= min && value <= max
                ? DataResult.success(reader.apply(value))
                : DataResult.error(() -> "Expected " + min + ".." + max), writer);
    }

    private NexusCodecs() {
    }
}
