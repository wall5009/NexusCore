package com.rollylindenshnizzer.nexuscore.compat;

import com.rollylindenshnizzer.nexuscore.core.NexusEnvironment;

import java.util.Collection;
import java.util.List;

public interface CompatModule {
    String modId();

    default Collection<String> dependencies() {
        return List.of(modId());
    }

    default boolean loaded() {
        for (String dependency : dependencies()) {
            if (!NexusEnvironment.isModLoaded(dependency)) {
                return false;
            }
        }
        return true;
    }

    void initialize();
}
