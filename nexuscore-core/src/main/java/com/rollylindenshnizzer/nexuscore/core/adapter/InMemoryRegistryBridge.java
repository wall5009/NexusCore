package com.rollylindenshnizzer.nexuscore.core.adapter;

import com.rollylindenshnizzer.nexuscore.api.platform.NexusTarget;
import com.rollylindenshnizzer.nexuscore.api.registry.NexusEntry;
import com.rollylindenshnizzer.nexuscore.api.registry.NexusFactory;
import com.rollylindenshnizzer.nexuscore.bridge.registry.RegistryBridge;
import com.rollylindenshnizzer.nexuscore.core.util.NexusErrorMessages;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class InMemoryRegistryBridge implements RegistryBridge {
    private final NexusTarget target;
    private final Map<String, Map<String, NexusEntry<?>>> registries = new LinkedHashMap<>();

    public InMemoryRegistryBridge(NexusTarget target) {
        this.target = target;
    }

    @Override
    public synchronized <T> NexusEntry<T> register(String registryName, String id, NexusFactory<T> factory) {
        Map<String, NexusEntry<?>> registry = registries.computeIfAbsent(registryName, ignored -> new LinkedHashMap<>());
        if (registry.containsKey(id)) {
            throw NexusErrorMessages.failure("register entry in " + registryName, id, target, "an entry with that id already exists", "give the entry a unique id or register it only once during initialization.");
        }
        T value = factory.create(new SimpleRegistryContext(registryName, id, target));
        NexusEntry<T> entry = new SimpleNexusEntry<>(id, value);
        registry.put(id, entry);
        return entry;
    }

    @Override
    public synchronized Optional<NexusEntry<?>> find(String registryName, String id) {
        return Optional.ofNullable(registries.getOrDefault(registryName, Map.of()).get(id));
    }

    @Override
    public synchronized List<NexusEntry<?>> entries(String registryName) {
        return new ArrayList<>(registries.getOrDefault(registryName, Map.of()).values());
    }
}
