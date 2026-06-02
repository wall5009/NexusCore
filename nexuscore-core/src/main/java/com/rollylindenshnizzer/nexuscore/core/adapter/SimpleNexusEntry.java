package com.rollylindenshnizzer.nexuscore.core.adapter;

import com.rollylindenshnizzer.nexuscore.api.registry.NexusEntry;

public final class SimpleNexusEntry<T> implements NexusEntry<T> {
    private final String id;
    private final T value;

    public SimpleNexusEntry(String id, T value) {
        this.id = id;
        this.value = value;
    }

    @Override
    public String id() {
        return id;
    }

    @Override
    public T get() {
        return value;
    }
}
