package com.rollylindenshnizzer.nexuscore.core;

import net.minecraft.resources.ResourceLocation;

public final class NexusResourcePaths {
    public static ResourceLocation texture(String modId, String path) {
        return NexusIds.id(modId, "textures/" + stripExtension(path, ".png") + ".png");
    }

    public static ResourceLocation itemTexture(String modId, String path) {
        return texture(modId, "item/" + path);
    }

    public static ResourceLocation blockTexture(String modId, String path) {
        return texture(modId, "block/" + path);
    }

    public static ResourceLocation model(String modId, String path) {
        return NexusIds.id(modId, stripExtension(path, ".json"));
    }

    public static ResourceLocation itemModel(String modId, String path) {
        return model(modId, "item/" + path);
    }

    public static ResourceLocation blockModel(String modId, String path) {
        return model(modId, "block/" + path);
    }

    public static ResourceLocation lootTable(String modId, String path) {
        return NexusIds.id(modId, "blocks/" + path);
    }

    public static ResourceLocation advancement(String modId, String path) {
        return NexusIds.id(modId, path);
    }

    private static String stripExtension(String path, String extension) {
        return path.endsWith(extension) ? path.substring(0, path.length() - extension.length()) : path;
    }

    private NexusResourcePaths() {
    }
}
