package com.rollylindenshnizzer.nexuscore.compat;

import com.rollylindenshnizzer.nexuscore.api.NexusDeprecated;
import com.rollylindenshnizzer.nexuscore.core.NexusEnvironment;

import java.util.Collection;
import java.util.List;

public interface CompatModule {
    default String id() {
        return modId();
    }

    @Deprecated(since = "1.1.0")
    String modId();

    default List<String> requiredMods() {
        return List.copyOf(dependencies());
    }

    @NexusDeprecated(since = "1.1.0", replaceWith = "com.rollylindenshnizzer.nexuscore.compat.CompatModule.requiredMods")
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
