package com.rollylindenshnizzer.nexuscore.core;

import net.minecraft.resources.ResourceLocation;

public final class MissingAssetException extends NexusException {
    public MissingAssetException(ResourceLocation asset, String suggestion) {
        super("Missing asset " + asset + ". " + suggestion);
    }
}
