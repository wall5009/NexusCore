package com.rollylindenshnizzer.nexuscore.compat.recipeviewer;

import net.minecraft.resources.ResourceLocation;

public record RecipeViewerDisplayPage(RecipeViewerCategory category,
                                      RecipeViewerDisplay display,
                                      RecipeViewerLayout layout,
                                      RecipeViewerPage page,
                                      int pageIndex) {
    public RecipeViewerDisplayPage {
        if (category == null) {
            throw new IllegalArgumentException("category cannot be null");
        }
        if (display == null) {
            throw new IllegalArgumentException("display cannot be null");
        }
        if (layout == null) {
            throw new IllegalArgumentException("layout cannot be null");
        }
        if (page == null) {
            throw new IllegalArgumentException("page cannot be null");
        }
        pageIndex = Math.max(0, pageIndex);
    }

    public ResourceLocation id() {
        if (pageIndex == 0) {
            return display.id();
        }
        return ResourceLocation.fromNamespaceAndPath(display.id().getNamespace(),
                display.id().getPath() + "_page_" + (pageIndex + 1));
    }
}
