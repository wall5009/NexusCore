package com.rollylindenshnizzer.nexuscore.core;

public final class RegistryTimingException extends NexusException {
    public RegistryTimingException(String modId, String registry, String path) {
        super("Invalid registry timing for " + modId + ":" + path + " in " + registry
                + ". Register content during mod initialization before registries are frozen.");
    }
}
