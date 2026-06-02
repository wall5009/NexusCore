package com.rollylindenshnizzer.nexuscore.core.bootstrap;

import com.rollylindenshnizzer.nexuscore.core.service.NexusServices;

public final class NexusCoreCommonBootstrap {
    private static boolean initialized;

    private NexusCoreCommonBootstrap() {
    }

    public static synchronized void init() {
        if (initialized) {
            return;
        }
        if (!NexusServices.isInstalled()) {
            throw new IllegalStateException("NexusCore common bootstrap cannot run before a target ServiceProvider is installed.");
        }
        initialized = true;
        NexusServices.get().events().fireCommonSetup();
    }

    public static synchronized void resetForTests() {
        initialized = false;
    }
}
