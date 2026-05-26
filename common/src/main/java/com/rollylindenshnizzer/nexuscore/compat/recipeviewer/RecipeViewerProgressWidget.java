package com.rollylindenshnizzer.nexuscore.compat.recipeviewer;

import net.minecraft.resources.ResourceLocation;

public record RecipeViewerProgressWidget(int x,
                                         int y,
                                         int width,
                                         int height,
                                         ResourceLocation texture,
                                         int u,
                                         int v,
                                         int regionWidth,
                                         int regionHeight,
                                         int textureWidth,
                                         int textureHeight,
                                         int durationMillis,
                                         RecipeViewerProgressDirection direction,
                                         boolean fullToEmpty) implements RecipeViewerWidget {
    public RecipeViewerProgressWidget {
        width = Math.max(1, width);
        height = Math.max(1, height);
        regionWidth = regionWidth <= 0 ? width : regionWidth;
        regionHeight = regionHeight <= 0 ? height : regionHeight;
        textureWidth = textureWidth <= 0 ? 256 : textureWidth;
        textureHeight = textureHeight <= 0 ? 256 : textureHeight;
        durationMillis = Math.max(0, durationMillis);
        direction = direction == null ? RecipeViewerProgressDirection.LEFT_TO_RIGHT : direction;
    }

    public static RecipeViewerProgressWidget arrow(int x, int y, int durationMillis) {
        return new RecipeViewerProgressWidget(x, y, 24, 17, null, 0, 0, 24, 17, 256, 256,
                durationMillis, RecipeViewerProgressDirection.LEFT_TO_RIGHT, false);
    }

    public static RecipeViewerProgressWidget texture(ResourceLocation texture, int x, int y, int width, int height,
                                                     int u, int v, int durationMillis) {
        return new RecipeViewerProgressWidget(x, y, width, height, texture, u, v, width, height, 256, 256,
                durationMillis, RecipeViewerProgressDirection.LEFT_TO_RIGHT, false);
    }
}
