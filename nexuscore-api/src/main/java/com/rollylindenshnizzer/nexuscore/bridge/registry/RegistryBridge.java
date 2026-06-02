package com.rollylindenshnizzer.nexuscore.bridge.registry;

import com.rollylindenshnizzer.nexuscore.api.registry.NexusEntry;
import com.rollylindenshnizzer.nexuscore.api.registry.NexusFactory;

import java.util.List;
import java.util.Optional;

public interface RegistryBridge {
    <T> NexusEntry<T> register(String registryName, String id, NexusFactory<T> factory);

    Optional<NexusEntry<?>> find(String registryName, String id);

    List<NexusEntry<?>> entries(String registryName);
}
