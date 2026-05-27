package com.rollylindenshnizzer.nexuscore.ui;

import java.util.Map;

public record WidgetDescriptor(String type, String binding, Map<String, String> properties) {
    public WidgetDescriptor {
        properties = properties == null ? Map.of() : Map.copyOf(properties);
    }

    public static WidgetDescriptor multiSelectDropdown(String binding) {
        return new WidgetDescriptor("multi_select_dropdown", binding, Map.of());
    }

    public static WidgetDescriptor registryPicker(String binding, String registry) {
        return new WidgetDescriptor("registry_entry_picker", binding, Map.of("registry", registry));
    }

    public static WidgetDescriptor tagPicker(String binding, String registry) {
        return new WidgetDescriptor("tag_picker", binding, Map.of("registry", registry));
    }

    public static WidgetDescriptor colorSwatches(String binding) {
        return new WidgetDescriptor("color_swatch_list", binding, Map.of());
    }

    public static WidgetDescriptor keyValueTable(String binding) {
        return new WidgetDescriptor("key_value_table", binding, Map.of());
    }
}
