package com.rollylindenshnizzer.nexuscore.machine;

import com.rollylindenshnizzer.nexuscore.api.NexusStable;
import com.rollylindenshnizzer.nexuscore.ui.WidgetDescriptor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@NexusStable(since = "1.2")
public record MachineScreenLayout(String template,
                                  List<WidgetDescriptor> widgets,
                                  Map<String, String> accessibilityLabels) {
    public MachineScreenLayout {
        template = template == null || template.isBlank() ? "machine/default" : template;
        widgets = widgets == null ? List.of() : List.copyOf(widgets);
        accessibilityLabels = accessibilityLabels == null ? Map.of() : Map.copyOf(accessibilityLabels);
    }

    public static Builder builder(String template) {
        return new Builder(template);
    }

    public static MachineScreenLayout generated(NexusMachineDefinition definition) {
        return builder("machine/generated")
                .widget(new WidgetDescriptor("progress_arrow", "machine.progress", Map.of("label", "Progress")))
                .widget(new WidgetDescriptor("energy_bar", "machine.energy", Map.of("label", "Energy")))
                .widget(new WidgetDescriptor("fluid_tank", "machine.fluid", Map.of("label", "Fluid")))
                .widget(new WidgetDescriptor("redstone_mode", "machine.redstone", Map.of("label", "Redstone")))
                .widget(new WidgetDescriptor("side_config", "machine.sides", Map.of("label", "Sides")))
                .accessibility("title", definition.id().toString())
                .build();
    }

    public static final class Builder {
        private final String template;
        private final List<WidgetDescriptor> widgets = new ArrayList<>();
        private final java.util.LinkedHashMap<String, String> accessibility = new java.util.LinkedHashMap<>();

        private Builder(String template) {
            this.template = template;
        }

        public Builder widget(WidgetDescriptor descriptor) {
            widgets.add(descriptor);
            return this;
        }

        public Builder accessibility(String key, String label) {
            accessibility.put(key, label);
            return this;
        }

        public MachineScreenLayout build() {
            return new MachineScreenLayout(template, widgets, accessibility);
        }
    }
}
