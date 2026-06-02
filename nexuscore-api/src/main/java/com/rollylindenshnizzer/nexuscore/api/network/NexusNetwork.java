package com.rollylindenshnizzer.nexuscore.api.network;

import com.rollylindenshnizzer.nexuscore.core.service.NexusServices;

import java.util.Objects;

public final class NexusNetwork {
    private final String modId;
    private final String channel;

    private NexusNetwork(String modId, String channel) {
        this.modId = Objects.requireNonNull(modId, "modId");
        this.channel = Objects.requireNonNull(channel, "channel");
    }

    public static NexusNetwork create(String modId, String channel) {
        return new NexusNetwork(modId, channel);
    }

    public String channelId() {
        return modId + ":" + channel;
    }

    public <P> PacketRegistrationBuilder<P> serverbound(String id, Class<P> packetType, NexusPacketDecoder<P> decoder) {
        return new PacketRegistrationBuilder<>(channelId(), id, packetType, decoder, NexusPacketDirection.SERVERBOUND);
    }

    public <P> PacketRegistrationBuilder<P> clientbound(String id, Class<P> packetType, NexusPacketDecoder<P> decoder) {
        return new PacketRegistrationBuilder<>(channelId(), id, packetType, decoder, NexusPacketDirection.CLIENTBOUND);
    }

    public void sendToServer(Object packet) {
        NexusServices.get().networking().sendToServer(packet);
    }

    public void sendToPlayer(Object player, Object packet) {
        NexusServices.get().networking().sendToPlayer(player, packet);
    }

    public static final class PacketRegistrationBuilder<P> {
        private final String channel;
        private final String id;
        private final Class<P> packetType;
        private final NexusPacketDecoder<P> decoder;
        private final NexusPacketDirection direction;
        private NexusPacketEncoder<P> encoder = (packet, buffer) -> {
            if (packet instanceof NexusPacket nexusPacket) {
                nexusPacket.encode(buffer);
            }
        };
        private NexusPacketHandler<P> handler = (packet, context) -> {
        };

        private PacketRegistrationBuilder(String channel, String id, Class<P> packetType, NexusPacketDecoder<P> decoder, NexusPacketDirection direction) {
            this.channel = channel;
            this.id = Objects.requireNonNull(id, "id");
            this.packetType = packetType;
            this.decoder = decoder;
            this.direction = direction;
        }

        public PacketRegistrationBuilder<P> handler(NexusPacketHandler<P> handler) {
            this.handler = Objects.requireNonNull(handler, "handler");
            return this;
        }

        public PacketRegistrationBuilder<P> encoder(NexusPacketEncoder<P> encoder) {
            this.encoder = Objects.requireNonNull(encoder, "encoder");
            return this;
        }

        public NetworkPacketDefinition<P> register() {
            NetworkPacketDefinition<P> definition = new NetworkPacketDefinition<>(channel, id, packetType, decoder, encoder, handler, direction);
            if (direction == NexusPacketDirection.SERVERBOUND) {
                NexusServices.get().networking().registerServerbound(definition);
            } else {
                NexusServices.get().networking().registerClientbound(definition);
            }
            return definition;
        }
    }
}
