package com.rollylindenshnizzer.nexuscore.registry;

import com.rollylindenshnizzer.nexuscore.core.NexusException;
import net.minecraft.resources.ResourceLocation;

public final class MissingRegistryEntryException extends NexusException {
    public MissingRegistryEntryException(ResourceLocation id, String reason) {
        super("Missing registry entry " + id + ". " + reason);
    }
}
