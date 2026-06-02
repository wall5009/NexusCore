package com.rollylindenshnizzer.nexuscore.api.platform;

import java.util.Locale;
import java.util.Objects;

public final class NexusTarget {
    private final NexusLoader loader;
    private final String minecraftVersion;
    private final String targetId;
    private final String mappings;
    private final NexusRuntimeSide side;

    public NexusTarget(NexusLoader loader, String minecraftVersion, String mappings, NexusRuntimeSide side) {
        this.loader = Objects.requireNonNull(loader, "loader");
        this.minecraftVersion = Objects.requireNonNull(minecraftVersion, "minecraftVersion");
        this.targetId = loader.id() + "_" + minecraftVersion.replace('.', '_').toLowerCase(Locale.ROOT);
        this.mappings = mappings == null || mappings.isBlank() ? "nexus-normalized" : mappings;
        this.side = side == null ? NexusRuntimeSide.COMMON : side;
    }

    public NexusLoader loader() {
        return loader;
    }

    public String minecraftVersion() {
        return minecraftVersion;
    }

    public String targetId() {
        return targetId;
    }

    public String mappings() {
        return mappings;
    }

    public NexusRuntimeSide side() {
        return side;
    }

    @Override
    public String toString() {
        return targetId;
    }
}
