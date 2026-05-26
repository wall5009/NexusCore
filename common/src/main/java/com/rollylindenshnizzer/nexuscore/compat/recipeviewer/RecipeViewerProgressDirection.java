package com.rollylindenshnizzer.nexuscore.compat.recipeviewer;

public enum RecipeViewerProgressDirection {
    LEFT_TO_RIGHT(true, false),
    RIGHT_TO_LEFT(true, true),
    TOP_TO_BOTTOM(false, false),
    BOTTOM_TO_TOP(false, true);

    private final boolean horizontal;
    private final boolean reverse;

    RecipeViewerProgressDirection(boolean horizontal, boolean reverse) {
        this.horizontal = horizontal;
        this.reverse = reverse;
    }

    public boolean horizontal() {
        return horizontal;
    }

    public boolean reverse() {
        return reverse;
    }
}
