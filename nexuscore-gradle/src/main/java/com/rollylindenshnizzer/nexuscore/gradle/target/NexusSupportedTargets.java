package com.rollylindenshnizzer.nexuscore.gradle.target;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class NexusSupportedTargets {
    private static final Map<String, List<String>> SUPPORTED = new LinkedHashMap<>();

    static {
        SUPPORTED.put("1.20.1", List.of("forge", "fabric", "quilt"));
        SUPPORTED.put("1.21.1", List.of("neoforge", "fabric", "quilt"));
        SUPPORTED.put("26.1.2", List.of("neoforge", "fabric"));
    }

    private NexusSupportedTargets() {
    }

    public static List<NexusTarget> all() {
        List<NexusTarget> targets = new ArrayList<>();
        SUPPORTED.forEach((version, loaders) -> loaders.forEach(loader -> targets.add(new NexusTarget(loader, version))));
        return targets;
    }

    public static void validate(NexusTarget target) {
        List<String> loaders = SUPPORTED.get(target.minecraftVersion());
        if (loaders == null) {
            throw new IllegalArgumentException("Minecraft " + target.minecraftVersion() + " is not supported by NexusCore v2.0. Supported versions are: " + SUPPORTED.keySet() + ".");
        }
        if (!loaders.contains(target.loader())) {
            throw new IllegalArgumentException(capitalized(target.loader()) + " " + target.minecraftVersion() + " is not supported by NexusCore v2.0. Supported " + target.minecraftVersion() + " loaders are: " + loaders + ".");
        }
    }

    public static String versionSourceSet(String minecraftVersion) {
        return "mc_" + minecraftVersion.replace('.', '_');
    }

    private static String capitalized(String value) {
        if (value == null || value.isBlank()) {
            return value;
        }
        String normalized = value.toLowerCase(Locale.ROOT);
        return normalized.substring(0, 1).toUpperCase(Locale.ROOT) + normalized.substring(1);
    }
}
