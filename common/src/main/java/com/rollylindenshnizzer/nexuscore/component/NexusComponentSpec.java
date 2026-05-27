package com.rollylindenshnizzer.nexuscore.component;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

public record NexusComponentSpec<T>(String path,
                                    DataComponentType<T> type,
                                    Optional<Supplier<T>> defaultValue,
                                    boolean persistent,
                                    boolean networkSynced,
                                    boolean cacheable,
                                    boolean recipeAware,
                                    ComponentCopyStrategy copyStrategy,
                                    List<Predicate<T>> validators,
                                    Optional<BiConsumer<T, List<Component>>> tooltipAppender) {
}
