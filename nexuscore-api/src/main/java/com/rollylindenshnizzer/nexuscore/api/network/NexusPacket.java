package com.rollylindenshnizzer.nexuscore.api.network;

public interface NexusPacket {
    default void encode(NexusPacketBuffer buffer) {
    }
}
