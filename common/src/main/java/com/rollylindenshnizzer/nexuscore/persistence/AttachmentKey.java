package com.rollylindenshnizzer.nexuscore.persistence;

import net.minecraft.resources.ResourceLocation;

public record AttachmentKey<T>(ResourceLocation id, Class<T> type, SyncPolicy syncPolicy, CopyPolicy copyPolicy) {
    public enum SyncPolicy {
        NEVER,
        TRACKING,
        OWNER,
        ALL
    }

    public enum CopyPolicy {
        NEVER,
        ON_DEATH,
        ON_DIMENSION_CHANGE,
        ALWAYS
    }
}
