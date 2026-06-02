package com.rollylindenshnizzer.nexuscore.gradle.target;

import java.util.Locale;

public record NexusTarget(String loader, String minecraftVersion) {
    public String targetId() {
        return loader + "_" + minecraftVersion.replace('.', '_');
    }

    public String sourceSetName() {
        return targetId();
    }

    public String taskSuffix() {
        return loaderDisplayName() + minecraftVersion.replace(".", "");
    }

    public String loaderDisplayName() {
        return switch (loader.toLowerCase(Locale.ROOT)) {
            case "fabric" -> "Fabric";
            case "forge" -> "Forge";
            case "neoforge" -> "NeoForge";
            case "quilt" -> "Quilt";
            default -> loader.substring(0, 1).toUpperCase(Locale.ROOT) + loader.substring(1);
        };
    }
}
