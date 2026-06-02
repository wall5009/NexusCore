package com.rollylindenshnizzer.nexuscore.api.event;

@FunctionalInterface
public interface PlayerJoinedCallback {
    void onPlayerJoined(NexusPlayer player);
}
