package com.rollylindenshnizzer.nexuscore.api.client;

import com.rollylindenshnizzer.nexuscore.api.event.ClientStartedCallback;
import com.rollylindenshnizzer.nexuscore.api.event.NexusEvents;
import com.rollylindenshnizzer.nexuscore.api.platform.NexusFeature;
import com.rollylindenshnizzer.nexuscore.api.platform.NexusPlatform;

public final class NexusClient {
    private NexusClient() {
    }

    public static boolean renderingAvailable() {
        return NexusPlatform.supports(NexusFeature.CLIENT_RENDERING);
    }

    public static void onStarted(ClientStartedCallback callback) {
        NexusEvents.CLIENT_STARTED.register(callback);
    }
}
