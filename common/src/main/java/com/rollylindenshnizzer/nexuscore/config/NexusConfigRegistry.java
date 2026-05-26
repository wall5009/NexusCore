package com.rollylindenshnizzer.nexuscore.config;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public final class NexusConfigRegistry {
    private static final Map<String, NexusConfig> CONFIGS = new LinkedHashMap<>();

    static void register(NexusConfig config) {
        CONFIGS.put(config.modId(), config);
    }

    public static Optional<NexusConfig> get(String modId) {
        return Optional.ofNullable(CONFIGS.get(modId));
    }

    public static Collection<NexusConfig> configs() {
        return Collections.unmodifiableCollection(CONFIGS.values());
    }

    private NexusConfigRegistry() {
    }
}
