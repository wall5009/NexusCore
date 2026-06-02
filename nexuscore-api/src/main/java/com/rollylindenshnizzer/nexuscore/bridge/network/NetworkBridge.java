package com.rollylindenshnizzer.nexuscore.bridge.network;

import com.rollylindenshnizzer.nexuscore.api.network.NetworkPacketDefinition;

import java.util.List;
import java.util.Optional;

public interface NetworkBridge {
    void registerServerbound(NetworkPacketDefinition<?> packet);

    void registerClientbound(NetworkPacketDefinition<?> packet);

    void sendToServer(Object packet);

    void sendToPlayer(Object player, Object packet);

    Optional<NetworkPacketDefinition<?>> find(String fullId);

    List<NetworkPacketDefinition<?>> packets();
}
