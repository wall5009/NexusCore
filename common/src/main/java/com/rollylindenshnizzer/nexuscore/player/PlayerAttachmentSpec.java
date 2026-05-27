package com.rollylindenshnizzer.nexuscore.player;

import com.rollylindenshnizzer.nexuscore.api.NexusStable;
import net.minecraft.resources.ResourceLocation;

@NexusStable(since = "1.2")
public record PlayerAttachmentSpec<T>(ResourceLocation id,
                                      T defaultValue,
                                      SyncPolicy syncPolicy,
                                      boolean copyOnDeath,
                                      boolean requireOperatorForClientWrites) {
    public PlayerAttachmentSpec {
        syncPolicy = syncPolicy == null ? SyncPolicy.SERVER_ONLY : syncPolicy;
    }

    public enum SyncPolicy {
        SERVER_ONLY,
        TRACKING_PLAYERS,
        OWNER_AND_TRACKING,
        OWNER_ONLY
    }
}
