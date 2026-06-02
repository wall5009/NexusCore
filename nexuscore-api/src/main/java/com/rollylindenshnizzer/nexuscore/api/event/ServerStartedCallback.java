package com.rollylindenshnizzer.nexuscore.api.event;

@FunctionalInterface
public interface ServerStartedCallback {
    void onServerStarted(Object server);
}
