package com.rollylindenshnizzer.nexuscore.component;

import com.mojang.serialization.Codec;
import com.rollylindenshnizzer.nexuscore.core.NexusIds;
import com.rollylindenshnizzer.nexuscore.registry.NexusRegistries;
import com.rollylindenshnizzer.nexuscore.registry.NexusRegistryGroup;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

public final class NexusComponentBuilder<T> {
    private final String modId;
    private final String path;
    private Codec<T> codec;
    private StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec;
    private Supplier<T> defaultValue;
    private boolean persistent;
    private boolean networkSynced;
    private boolean cacheable;
    private boolean recipeAware;
    private ComponentCopyStrategy copyStrategy = ComponentCopyStrategy.ALWAYS;
    private final List<Predicate<T>> validators = new ArrayList<>();
    private BiConsumer<T, List<Component>> tooltipAppender;

    NexusComponentBuilder(String modId, String path) {
        this.modId = NexusIds.requireNamespace(modId);
        this.path = NexusIds.normalizePath(path);
    }

    public NexusComponentBuilder<T> codec(Codec<T> codec) {
        this.codec = codec;
        this.persistent = true;
        return this;
    }

    public NexusComponentBuilder<T> streamCodec(StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec) {
        this.streamCodec = streamCodec;
        this.networkSynced = true;
        return this;
    }

    public NexusComponentBuilder<T> defaultValue(Supplier<T> defaultValue) {
        this.defaultValue = defaultValue;
        return this;
    }

    public NexusComponentBuilder<T> defaultValue(T defaultValue) {
        this.defaultValue = () -> defaultValue;
        return this;
    }

    public NexusComponentBuilder<T> persistent() {
        this.persistent = true;
        return this;
    }

    public NexusComponentBuilder<T> networkSynced() {
        this.networkSynced = true;
        return this;
    }

    public NexusComponentBuilder<T> cacheable() {
        this.cacheable = true;
        return this;
    }

    public NexusComponentBuilder<T> recipeAware() {
        this.recipeAware = true;
        return this;
    }

    public NexusComponentBuilder<T> copyStrategy(ComponentCopyStrategy copyStrategy) {
        this.copyStrategy = copyStrategy;
        return this;
    }

    public NexusComponentBuilder<T> validator(Predicate<T> validator) {
        validators.add(validator);
        return this;
    }

    public NexusComponentBuilder<T> tooltip(BiConsumer<T, List<Component>> tooltipAppender) {
        this.tooltipAppender = tooltipAppender;
        return this;
    }

    public RegistrySupplier<DataComponentType<T>> register() {
        return register(NexusRegistries.group(modId));
    }

    public RegistrySupplier<DataComponentType<T>> register(NexusRegistryGroup group) {
        return group.dataComponent(path, this::build);
    }

    public DataComponentType<T> build() {
        DataComponentType.Builder<T> builder = DataComponentType.builder();
        if (codec != null && persistent) {
            builder.persistent(codec);
        }
        if (streamCodec != null && networkSynced) {
            builder.networkSynchronized(streamCodec);
        }
        if (cacheable) {
            builder.cacheEncoding();
        }
        return builder.build();
    }

    public NexusComponentSpec<T> spec(DataComponentType<T> type) {
        return new NexusComponentSpec<>(path, type, Optional.ofNullable(defaultValue), persistent, networkSynced,
                cacheable, recipeAware, copyStrategy, List.copyOf(validators), Optional.ofNullable(tooltipAppender));
    }
}
