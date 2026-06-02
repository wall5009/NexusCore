package com.rollylindenshnizzer.nexuscore.api;

import com.rollylindenshnizzer.nexuscore.api.platform.NexusPlatform;
import com.rollylindenshnizzer.nexuscore.core.service.NexusServices;

import java.util.Locale;
import java.util.Objects;

public final class NexusMod {
    private final String modId;

    private NexusMod(String modId) {
        String normalized = Objects.requireNonNull(modId, "modId").toLowerCase(Locale.ROOT);
        if (!normalized.matches("[a-z][a-z0-9_\\-]{1,63}")) {
            throw new IllegalArgumentException("Invalid NexusCore mod id '" + modId + "'. Use lowercase letters, numbers, underscores, or dashes.");
        }
        this.modId = normalized;
    }

    public static NexusMod create(String modId) {
        return new NexusMod(modId);
    }

    public String id() {
        return modId;
    }

    public NexusLogger logger() {
        return NexusServices.get().logger(modId);
    }

    public NexusPlatform platform() {
        return new NexusPlatform();
    }

    public NexusPaths paths() {
        return NexusServices.get().paths();
    }

    @Override
    public String toString() {
        return "NexusMod[" + modId + "]";
    }
}
