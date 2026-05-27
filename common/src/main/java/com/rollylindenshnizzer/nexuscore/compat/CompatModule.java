package com.rollylindenshnizzer.nexuscore.compat;

import com.rollylindenshnizzer.nexuscore.core.NexusEnvironment;

import java.util.Collection;
import java.util.List;

public interface CompatModule {
    default String id() {
        return modId();
    }

    String modId();

    default List<String> requiredMods() {
        return List.copyOf(dependencies());
    }

    default Collection<String> dependencies() {
        return requiredMods();
    }

    default boolean enabled() {
        return true;
    }

    default boolean loaded() {
        return enabled() && requiredMods().stream()
                .allMatch(NexusEnvironment::isModLoaded);
    }

    void initialize();
}
