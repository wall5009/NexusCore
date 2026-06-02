package com.rollylindenshnizzer.nexuscore.api.network;

@FunctionalInterface
public interface NexusPacketEncoder<P> {
    void encode(P packet, NexusPacketBuffer buffer);
}
