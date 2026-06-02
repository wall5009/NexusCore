package com.rollylindenshnizzer.nexuscore.api.registry;

@FunctionalInterface
public interface NexusFactory<T> {
    T create(NexusRegistryContext context);
}
