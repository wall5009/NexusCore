package com.rollylindenshnizzer.nexuscore.core.adapter;

import com.rollylindenshnizzer.nexuscore.api.platform.NexusTarget;
import com.rollylindenshnizzer.nexuscore.api.registry.NexusRegistryContext;

public final class SimpleRegistryContext implements NexusRegistryContext {
    private final String registryName;
    private final String id;
    private final NexusTarget target;

    public SimpleRegistryContext(String registryName, String id, NexusTarget target) {
        this.registryName = registryName;
        this.id = id;
        this.target = target;
    }

    @Override
    public String registryName() {
        return registryName;
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public NexusTarget target() {
        return target;
    }
}
