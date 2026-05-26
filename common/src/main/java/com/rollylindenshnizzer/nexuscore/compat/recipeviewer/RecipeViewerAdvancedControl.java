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

    private static String normalize(String value, String fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        return value.trim().toLowerCase();
    }
}
