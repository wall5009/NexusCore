package com.rollylindenshnizzer.nexuscore.compat.recipeviewer;

import java.util.Map;
import java.util.Set;

public final class RecipeViewerControlSupport {
    private static final Map<String, Set<String>> NATIVE_SUPPORT = Map.of(
            "jei", Set.of("tooltip", "recipe_transfer_button", "shapeless", "badge"),
            "emi", Set.of("tooltip", "button", "recipe_tree", "hide_craftable", "badge"),
            "rei", Set.of("tooltip", "button", "badge")
    );

    public static boolean supports(String viewer, RecipeViewerAdvancedControl control) {
        return NATIVE_SUPPORT.getOrDefault(normalize(viewer), Set.of()).contains(control.type());
    }

    public static Set<String> supportedTypes(String viewer) {
        return NATIVE_SUPPORT.getOrDefault(normalize(viewer), Set.of());
    }

    public static String fallbackTooltip(String viewer, RecipeViewerAdvancedControl control) {
        if (supports(viewer, control)) {
            return "";
        }
        String fallback = control.property("fallback", "");
        if (!fallback.isBlank()) {
            return fallback;
        }
        return control.type() + " is not a native " + normalize(viewer).toUpperCase() + " control; NexusCore rendered a safe fallback.";
    }

    public static void requireSupported(String viewer, RecipeViewerAdvancedControl control) {
        if (control.strict() && !supports(viewer, control)) {
            throw new IllegalArgumentException("Recipe viewer control " + control.type()
                    + " is not supported by " + normalize(viewer));
        }
    }

    private static String normalize(String viewer) {
        return viewer == null || viewer.isBlank() ? "all" : viewer.trim().toLowerCase();
    }

    private RecipeViewerControlSupport() {
    }
}
