package com.rollylindenshnizzer.nexuscore.compat.recipeviewer;

import net.minecraft.network.chat.Component;

public record RecipeViewerTextWidget(Component text, int x, int y, int color, boolean shadow) implements RecipeViewerWidget {
    public RecipeViewerTextWidget {
        text = text == null ? Component.empty() : text;
    }
}
