package com.rollylindenshnizzer.nexuscore.core.adapter;

import com.rollylindenshnizzer.nexuscore.api.event.ClientStartedCallback;
import com.rollylindenshnizzer.nexuscore.api.event.CommonSetupCallback;
import com.rollylindenshnizzer.nexuscore.api.event.NexusPlayer;
import com.rollylindenshnizzer.nexuscore.api.event.PlayerJoinedCallback;
import com.rollylindenshnizzer.nexuscore.api.event.ServerStartedCallback;
import com.rollylindenshnizzer.nexuscore.api.event.TickCallback;
import com.rollylindenshnizzer.nexuscore.bridge.event.EventBridge;

import java.util.ArrayList;
import java.util.List;

public class InMemoryEventBridge implements EventBridge {
    private final List<CommonSetupCallback> commonSetup = new ArrayList<>();
    private final List<ServerStartedCallback> serverStarted = new ArrayList<>();
    private final List<PlayerJoinedCallback> playerJoined = new ArrayList<>();
    private final List<ClientStartedCallback> clientStarted = new ArrayList<>();
    private final List<TickCallback> serverTick = new ArrayList<>();
    private final List<TickCallback> clientTick = new ArrayList<>();

    @Override
    public void registerCommonSetup(CommonSetupCallback callback) {
        commonSetup.add(callback);
    }

    @Override
    public void registerServerStarted(ServerStartedCallback callback) {
        serverStarted.add(callback);
    }

    @Override
    public void registerPlayerJoined(PlayerJoinedCallback callback) {
        playerJoined.add(callback);
    }

    @Override
    public void registerClientStarted(ClientStartedCallback callback) {
        clientStarted.add(callback);
    }

    @Override
    public void registerServerTick(TickCallback callback) {
        serverTick.add(callback);
    }

    @Override
    public void registerClientTick(TickCallback callback) {
        clientTick.add(callback);
    }

    @Override
    public void fireCommonSetup() {
        commonSetup.forEach(CommonSetupCallback::onCommonSetup);
    }

    @Override
    public void fireServerStarted(Object server) {
        serverStarted.forEach(callback -> callback.onServerStarted(server));
    }

    @Override
    public void firePlayerJoined(NexusPlayer player) {
        playerJoined.forEach(callback -> callback.onPlayerJoined(player));
    }

    @Override
    public void fireClientStarted(Object client) {
        clientStarted.forEach(callback -> callback.onClientStarted(client));
    }

    @Override
    public void fireServerTick(long tick) {
        serverTick.forEach(callback -> callback.onTick(tick));
    }

    @Override
    public void fireClientTick(long tick) {
        clientTick.forEach(callback -> callback.onTick(tick));
    }
}
