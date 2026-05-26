package com.rollylindenshnizzer.nexuscore.core;

import net.minecraft.resources.ResourceLocation;

public final class PacketValidationException extends NexusException {
    public PacketValidationException(ResourceLocation packetId, String reason) {
        super("Packet " + packetId + " failed validation: " + reason);
    }
}
