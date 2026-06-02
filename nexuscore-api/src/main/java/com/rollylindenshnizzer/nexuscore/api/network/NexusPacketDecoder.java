package com.rollylindenshnizzer.nexuscore.api.network;

@FunctionalInterface
public interface NexusPacketDecoder<P> {
    P decode(NexusPacketBuffer buffer);
}
