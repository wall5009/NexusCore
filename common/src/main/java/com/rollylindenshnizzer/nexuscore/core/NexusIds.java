package com.rollylindenshnizzer.nexuscore.core;

import net.minecraft.resources.ResourceLocation;

import java.util.Locale;

public final class NexusIds {
    public static ResourceLocation id(String modId, String path) {
        return ResourceLocation.fromNamespaceAndPath(requireNamespace(modId), normalizePath(path));
    }

    public static ResourceLocation parse(String value) {
        ResourceLocation id = ResourceLocation.tryParse(value);
        if (id == null) {
            throw new NexusException("Invalid resource location: " + value);
        }
        return id;
    }

    public static String requireNamespace(String namespace) {
        String value = namespace == null ? "" : namespace.trim().toLowerCase(Locale.ROOT);
        if (!ResourceLocation.isValidNamespace(value)) {
            throw new NexusException("Invalid namespace '" + namespace + "'. Use lowercase letters, numbers, '_', '-' or '.'.");
        }
        return value;
    }

    public static String normalizePath(String path) {
        String value = path == null ? "" : path.trim().replace('\\', '/').toLowerCase(Locale.ROOT);
        while (value.startsWith("/")) {
            value = value.substring(1);
        }
        if (!ResourceLocation.isValidPath(value)) {
            throw new NexusException("Invalid resource path '" + path + "'. Use lowercase letters, numbers, '/', '_', '-' or '.'.");
        }
        return value;
    }

    public static String translationKey(String prefix, ResourceLocation id) {
        return prefix + "." + id.getNamespace() + "." + id.getPath().replace('/', '.');
    }

    public static String humanName(String path) {
        String[] words = normalizePath(path).replace('/', '_').split("_");
        StringBuilder builder = new StringBuilder();
        for (String word : words) {
            if (word.isEmpty()) {
                continue;
            }
            if (!builder.isEmpty()) {
                builder.append(' ');
            }
            builder.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
        }
        return builder.toString();
    }

    private NexusIds() {
    }
}
