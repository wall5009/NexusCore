package com.rollylindenshnizzer.nexuscore.core.target;

import com.rollylindenshnizzer.nexuscore.api.platform.NexusLoader;
import com.rollylindenshnizzer.nexuscore.api.platform.NexusRuntimeSide;
import com.rollylindenshnizzer.nexuscore.api.platform.NexusTarget;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public final class NexusTargets {
    private static final Map<String, List<NexusLoader>> SUPPORTED = new LinkedHashMap<>();

    static {
        SUPPORTED.put("1.20.1", List.of(NexusLoader.FORGE, NexusLoader.FABRIC, NexusLoader.QUILT));
        SUPPORTED.put("1.21.1", List.of(NexusLoader.NEOFORGE, NexusLoader.FABRIC, NexusLoader.QUILT));
        SUPPORTED.put("26.1.2", List.of(NexusLoader.NEOFORGE, NexusLoader.FABRIC));
    }

    private NexusTargets() {
    }

    public static List<String> targetIds() {
        List<String> ids = new ArrayList<>();
        SUPPORTED.forEach((version, loaders) -> loaders.forEach(loader -> ids.add(id(loader, version))));
        return ids;
    }

    public static boolean supports(NexusLoader loader, String minecraftVersion) {
        return SUPPORTED.getOrDefault(minecraftVersion, List.of()).contains(loader);
    }

    public static NexusTarget target(NexusLoader loader, String minecraftVersion) {
        validate(loader, minecraftVersion);
        return new NexusTarget(loader, minecraftVersion, "nexus-normalized", NexusRuntimeSide.COMMON);
    }

    public static Optional<NexusTarget> byId(String targetId) {
        return SUPPORTED.entrySet().stream()
            .flatMap(entry -> entry.getValue().stream().map(loader -> target(loader, entry.getKey())))
            .filter(target -> target.targetId().equals(targetId))
            .findFirst();
    }

    public static void validate(NexusLoader loader, String minecraftVersion) {
        List<NexusLoader> loaders = SUPPORTED.get(minecraftVersion);
        if (loaders == null) {
            throw new IllegalArgumentException("Minecraft " + minecraftVersion + " is not supported by NexusCore v2.0. Supported versions are: " + SUPPORTED.keySet() + ".");
        }
        if (!loaders.contains(loader)) {
            throw new IllegalArgumentException(capitalized(loader.id()) + " " + minecraftVersion + " is not supported by NexusCore v2.0. Supported " + minecraftVersion + " loaders are: " + loaders.stream().map(NexusLoader::id).toList() + ".");
        }
    }

    public static String id(NexusLoader loader, String minecraftVersion) {
        return loader.id() + "_" + minecraftVersion.replace('.', '_').toLowerCase(Locale.ROOT);
    }

    private static String capitalized(String value) {
        if (value.isEmpty()) {
            return value;
        }
        return value.substring(0, 1).toUpperCase(Locale.ROOT) + value.substring(1);
    }
}
