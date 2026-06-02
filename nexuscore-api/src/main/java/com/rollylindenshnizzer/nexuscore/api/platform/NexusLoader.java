package com.rollylindenshnizzer.nexuscore.api.platform;

import java.util.Locale;

public enum NexusLoader {
    FABRIC("fabric"),
    FORGE("forge"),
    NEOFORGE("neoforge"),
    QUILT("quilt"),
    UNKNOWN("unknown");

    private final String id;

    NexusLoader(String id) {
        this.id = id;
    }

    public String id() {
        return id;
    }

    public static NexusLoader fromId(String id) {
        String normalized = id == null ? "" : id.toLowerCase(Locale.ROOT);
        for (NexusLoader loader : values()) {
            if (loader.id.equals(normalized)) {
                return loader;
            }
        }
        return UNKNOWN;
    }
}
