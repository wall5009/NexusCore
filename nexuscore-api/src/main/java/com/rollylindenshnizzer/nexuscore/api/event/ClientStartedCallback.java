package com.rollylindenshnizzer.nexuscore.api.event;

@FunctionalInterface
public interface ClientStartedCallback {
    void onClientStarted(Object client);
}
