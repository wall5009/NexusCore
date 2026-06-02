package com.rollylindenshnizzer.nexuscore.core.service;

import java.util.Objects;

public final class NexusServices {
    private static volatile ServiceProvider provider;

    private NexusServices() {
    }

    public static synchronized void install(ServiceProvider serviceProvider) {
        provider = Objects.requireNonNull(serviceProvider, "serviceProvider");
    }

    public static boolean isInstalled() {
        return provider != null;
    }

    public static ServiceProvider get() {
        ServiceProvider current = provider;
        if (current == null) {
            throw new IllegalStateException("NexusCore services have not been installed yet. Install a target ServiceProvider from the loader entrypoint before using NexusCore APIs.");
        }
        return current;
    }

    public static synchronized void clearForTests() {
        provider = null;
    }
}
