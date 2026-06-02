package com.rollylindenshnizzer.nexuscore.bridge.event;

import com.rollylindenshnizzer.nexuscore.api.event.ClientStartedCallback;
import com.rollylindenshnizzer.nexuscore.api.event.CommonSetupCallback;
import com.rollylindenshnizzer.nexuscore.api.event.NexusPlayer;
import com.rollylindenshnizzer.nexuscore.api.event.PlayerJoinedCallback;
import com.rollylindenshnizzer.nexuscore.api.event.ServerStartedCallback;
import com.rollylindenshnizzer.nexuscore.api.event.TickCallback;

public interface EventBridge {
    void registerCommonSetup(CommonSetupCallback callback);

    void registerServerStarted(ServerStartedCallback callback);

    void registerPlayerJoined(PlayerJoinedCallback callback);

    void registerClientStarted(ClientStartedCallback callback);

    void registerServerTick(TickCallback callback);

    void registerClientTick(TickCallback callback);

    void fireCommonSetup();

    void fireServerStarted(Object server);

    void firePlayerJoined(NexusPlayer player);

    void fireClientStarted(Object client);

    void fireServerTick(long tick);

    void fireClientTick(long tick);
}
