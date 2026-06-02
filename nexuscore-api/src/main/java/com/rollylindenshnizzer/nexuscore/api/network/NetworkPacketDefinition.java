package com.rollylindenshnizzer.nexuscore.api.network;

import java.util.Objects;

public final class NetworkPacketDefinition<P> {
    private final String channel;
    private final String id;
    private final Class<P> packetType;
    private final NexusPacketDecoder<P> decoder;
    private final NexusPacketEncoder<P> encoder;
    private final NexusPacketHandler<P> handler;
    private final NexusPacketDirection direction;

    public NetworkPacketDefinition(String channel, String id, Class<P> packetType, NexusPacketDecoder<P> decoder, NexusPacketHandler<P> handler, NexusPacketDirection direction) {
        this(channel, id, packetType, decoder, defaultEncoder(), handler, direction);
    }

    public NetworkPacketDefinition(String channel, String id, Class<P> packetType, NexusPacketDecoder<P> decoder, NexusPacketEncoder<P> encoder, NexusPacketHandler<P> handler, NexusPacketDirection direction) {
        this.channel = Objects.requireNonNull(channel, "channel");
        this.id = Objects.requireNonNull(id, "id");
        this.packetType = Objects.requireNonNull(packetType, "packetType");
        this.decoder = Objects.requireNonNull(decoder, "decoder");
        this.encoder = Objects.requireNonNull(encoder, "encoder");
        this.handler = Objects.requireNonNull(handler, "handler");
        this.direction = Objects.requireNonNull(direction, "direction");
    }

    public String channel() {
        return channel;
    }

    public String id() {
        return id;
    }

    public String fullId() {
        return channel + "/" + id;
    }

    public Class<P> packetType() {
        return packetType;
    }

    public NexusPacketDecoder<P> decoder() {
        return decoder;
    }

    public NexusPacketEncoder<P> encoder() {
        return encoder;
    }

    public NexusPacketHandler<P> handler() {
        return handler;
    }

    public NexusPacketDirection direction() {
        return direction;
    }

    @SuppressWarnings("unchecked")
    private static <P> NexusPacketEncoder<P> defaultEncoder() {
        return (packet, buffer) -> {
            if (packet instanceof NexusPacket nexusPacket) {
                nexusPacket.encode(buffer);
            }
        };
    }
}
