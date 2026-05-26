package com.rollylindenshnizzer.nexuscore.registry;

import java.util.Collection;
import java.util.List;

public interface ContentModule {
    String id();

    default boolean enabled() {
        return true;
    }

    default Collection<String> dependencies() {
        return List.of();
    }

    default void register(NexusRegistryGroup registries) {
    }

    default void dataGeneration() {
    }

    default void compatibility() {
    }
}
