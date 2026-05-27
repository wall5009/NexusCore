package com.rollylindenshnizzer.nexuscore.compat.recipeviewer;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public record RecipeViewerAdvancedControl(String viewer, String type, Map<String, String> properties) {
    public RecipeViewerAdvancedControl {
        viewer = normalize(viewer, "all");
        type = normalize(type, "custom");
        properties = properties == null ? Map.of() : Map.copyOf(new LinkedHashMap<>(properties));
    }

    public RecipeViewerAdvancedControl(String viewer, String type) {
        this(viewer, type, Map.of());
    }

    public static RecipeViewerAdvancedControl tooltip(String viewer, int x, int y, int width, int height, String text) {
        return new RecipeViewerAdvancedControl(viewer, "tooltip", Map.of(
                "x", Integer.toString(x),
                "y", Integer.toString(y),
                "width", Integer.toString(width),
                "height", Integer.toString(height),
                "text", text));
    }

    public static RecipeViewerAdvancedControl button(String viewer, int x, int y, int width, int height, String text) {
        return new RecipeViewerAdvancedControl(viewer, "button", Map.of(
                "x", Integer.toString(x),
                "y", Integer.toString(y),
                "width", Integer.toString(width),
                "height", Integer.toString(height),
                "text", text,
                "fallback", text));
    }

    public static RecipeViewerAdvancedControl recipeTransferButton(int x, int y) {
        return new RecipeViewerAdvancedControl("all", "recipe_transfer_button", Map.of(
                "x", Integer.toString(x),
                "y", Integer.toString(y),
                "width", "24",
                "height", "18",
                "fallback", "Use the recipe transfer controls supplied by your recipe viewer."));
    }

    public static RecipeViewerAdvancedControl recipeTree(boolean enabled) {
        return new RecipeViewerAdvancedControl("all", "recipe_tree", Map.of("enabled", Boolean.toString(enabled),
                "fallback", enabled ? "Recipe tree is enabled where supported." : "Recipe tree is disabled where supported."));
    }

    public static RecipeViewerAdvancedControl hideCraftable(boolean enabled) {
        return new RecipeViewerAdvancedControl("all", "hide_craftable", Map.of("enabled", Boolean.toString(enabled),
                "fallback", enabled ? "Craftable recipes are hidden where supported." : "Craftable recipes are visible where supported."));
    }

    public static RecipeViewerAdvancedControl shapeless() {
        return new RecipeViewerAdvancedControl("all", "shapeless", Map.of("fallback", "This recipe is shapeless."));
    }

    public static RecipeViewerAdvancedControl badge(String viewer, int x, int y, String text) {
        return new RecipeViewerAdvancedControl(viewer, "badge", Map.of(
                "x", Integer.toString(x),
                "y", Integer.toString(y),
                "width", Integer.toString(Math.max(18, text.length() * 6 + 8)),
                "height", "12",
                "text", text,
                "fallback", text));
    }

    public boolean appliesTo(String viewerId) {
        return "all".equals(viewer) || Objects.equals(viewer, normalize(viewerId, ""));
    }

    public String property(String key, String fallback) {
        return properties.getOrDefault(key, fallback);
    }

    public int intProperty(String key, int fallback) {
        String value = properties.get(key);
        if (value == null) {
            return fallback;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ignored) {
            return fallback;
        }
    }

    public boolean booleanProperty(String key, boolean fallback) {
        String value = properties.get(key);
        return value == null ? fallback : Boolean.parseBoolean(value);
    }

    public String fallbackText(String viewerId) {
        String fallback = property("fallback", "");
        return fallback.isBlank() ? RecipeViewerControlSupport.fallbackTooltip(viewerId, this) : fallback;
    }

    public boolean strict() {
        return booleanProperty("strict", false);
    }

    private static String normalize(String value, String fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        return value.trim().toLowerCase();
    }
}
