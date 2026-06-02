package com.rollylindenshnizzer.nexuscore.api.registry;

public interface NexusEntry<T> {
    String id();

    T get();
}
