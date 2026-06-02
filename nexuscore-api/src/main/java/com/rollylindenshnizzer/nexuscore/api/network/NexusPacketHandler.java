package com.rollylindenshnizzer.nexuscore.api.network;

@FunctionalInterface
public interface NexusPacketHandler<P> {
    void handle(P packet, NexusPacketContext context);
}
