package com.rollylindenshnizzer.nexuscore.api.lifecycle;

import com.rollylindenshnizzer.nexuscore.api.event.CommonSetupCallback;
import com.rollylindenshnizzer.nexuscore.api.event.NexusEvents;
import com.rollylindenshnizzer.nexuscore.api.event.ServerStartedCallback;

public final class NexusLifecycle {
    private NexusLifecycle() {
    }

    public static void commonSetup(CommonSetupCallback callback) {
        NexusEvents.COMMON_SETUP.register(callback);
    }

    public static void serverStarted(ServerStartedCallback callback) {
        NexusEvents.SERVER_STARTED.register(callback);
    }
}
