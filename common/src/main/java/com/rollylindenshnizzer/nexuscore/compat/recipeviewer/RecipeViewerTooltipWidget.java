package com.rollylindenshnizzer.nexuscore.compat.recipeviewer;

import net.minecraft.network.chat.Component;

import java.util.List;

public record RecipeViewerTooltipWidget(int x, int y, int width, int height,
                                        List<Component> lines) implements RecipeViewerWidget {
    public RecipeViewerTooltipWidget {
        width = Math.max(1, width);
        height = Math.max(1, height);
        lines = lines == null ? List.of() : List.copyOf(lines);
    }

    public boolean contains(double mouseX, double mouseY) {
        return mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;
    }
}
