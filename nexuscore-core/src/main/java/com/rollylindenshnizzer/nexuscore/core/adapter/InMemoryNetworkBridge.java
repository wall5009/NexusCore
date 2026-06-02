package com.rollylindenshnizzer.nexuscore.core.adapter;

import com.rollylindenshnizzer.nexuscore.api.event.NexusPlayer;
import com.rollylindenshnizzer.nexuscore.api.network.NetworkPacketDefinition;
import com.rollylindenshnizzer.nexuscore.api.network.NexusPacketDirection;
import com.rollylindenshnizzer.nexuscore.api.platform.NexusRuntimeSide;
import com.rollylindenshnizzer.nexuscore.bridge.network.NetworkBridge;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class InMemoryNetworkBridge implements NetworkBridge {
    private final Map<String, NetworkPacketDefinition<?>> packets = new LinkedHashMap<>();
    private final List<Object> serverboundSent = new ArrayList<>();
    private final List<Object> clientboundSent = new ArrayList<>();

    @Override
    public void registerServerbound(NetworkPacketDefinition<?> packet) {
        register(packet, NexusPacketDirection.SERVERBOUND);
    }

    @Override
    public void registerClientbound(NetworkPacketDefinition<?> packet) {
        register(packet, NexusPacketDirection.CLIENTBOUND);
    }

    @Override
    public void sendToServer(Object packet) {
        serverboundSent.add(packet);
    }

    @Override
    public void sendToPlayer(Object player, Object packet) {
        clientboundSent.add(packet);
        if (player instanceof NexusPlayer nexusPlayer) {
            nexusPlayer.sendMessage("packet:" + packet.getClass().getSimpleName());
        }
    }

    @Override
    public Optional<NetworkPacketDefinition<?>> find(String fullId) {
        return Optional.ofNullable(packets.get(fullId));
    }

    @Override
    public List<NetworkPacketDefinition<?>> packets() {
        return new ArrayList<>(packets.values());
    }

    public List<Object> serverboundSent() {
        return new ArrayList<>(serverboundSent);
    }

    public List<Object> clientboundSent() {
        return new ArrayList<>(clientboundSent);
    }

    public void handle(String fullId, Object packet, NexusPlayer player, NexusRuntimeSide side) {
        NetworkPacketDefinition<?> definition = packets.get(fullId);
        if (definition != null) {
            handleUnchecked(definition, packet, player, side);
        }
    }

    private void register(NetworkPacketDefinition<?> packet, NexusPacketDirection direction) {
        if (packet.direction() != direction) {
            throw new IllegalArgumentException("packet direction mismatch for " + packet.fullId());
        }
        if (packets.putIfAbsent(packet.fullId(), packet) != null) {
            throw new IllegalStateException("network packet is already registered: " + packet.fullId());
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void handleUnchecked(NetworkPacketDefinition definition, Object packet, NexusPlayer player, NexusRuntimeSide side) {
        definition.handler().handle(packet, new SimplePacketContext(player, side));
    }
}
