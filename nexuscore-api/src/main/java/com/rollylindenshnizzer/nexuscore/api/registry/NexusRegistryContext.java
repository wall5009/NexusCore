package com.rollylindenshnizzer.nexuscore.api.registry;

import com.rollylindenshnizzer.nexuscore.api.platform.NexusTarget;

public interface NexusRegistryContext {
    String registryName();

    String id();

    NexusTarget target();
}
