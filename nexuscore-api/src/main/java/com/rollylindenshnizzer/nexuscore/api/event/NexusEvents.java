package com.rollylindenshnizzer.nexuscore.api.event;

import com.rollylindenshnizzer.nexuscore.bridge.event.EventBridge;
import com.rollylindenshnizzer.nexuscore.core.service.NexusServices;
import com.rollylindenshnizzer.nexuscore.core.service.ServiceProvider;

import java.util.Objects;
import java.util.function.BiConsumer;

public final class NexusEvents {
    public static final NexusEvent<CommonSetupCallback> COMMON_SETUP = new BridgeEvent<>("common_setup", EventBridge::registerCommonSetup);
    public static final NexusEvent<ServerStartedCallback> SERVER_STARTED = new BridgeEvent<>("server_started", EventBridge::registerServerStarted);
    public static final NexusEvent<PlayerJoinedCallback> PLAYER_JOINED = new BridgeEvent<>("player_joined", EventBridge::registerPlayerJoined);
    public static final NexusEvent<ClientStartedCallback> CLIENT_STARTED = new BridgeEvent<>("client_started", EventBridge::registerClientStarted);
    public static final NexusEvent<TickCallback> SERVER_TICK = new BridgeEvent<>("server_tick", EventBridge::registerServerTick);
    public static final NexusEvent<TickCallback> CLIENT_TICK = new BridgeEvent<>("client_tick", EventBridge::registerClientTick);

    private NexusEvents() {
    }

    private static final class BridgeEvent<T> implements NexusEvent<T> {
        private final String name;
        private final BiConsumer<EventBridge, T> registrar;

        private BridgeEvent(String name, BiConsumer<EventBridge, T> registrar) {
            this.name = name;
            this.registrar = registrar;
        }

        @Override
        public void register(T listener) {
            ServiceProvider provider = NexusServices.get();
            try {
                registrar.accept(provider.events(), Objects.requireNonNull(listener, "listener"));
            } catch (RuntimeException error) {
                throw new IllegalStateException("NexusCore could not register event listener for '" + name + "'. Target: " + provider.target().targetId() + ". Reason: " + error.getMessage() + ". Fix: register events during common, client, or server initialization.", error);
            }
        }
    }
}
