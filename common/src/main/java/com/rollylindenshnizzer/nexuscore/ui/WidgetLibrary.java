package com.rollylindenshnizzer.nexuscore.ui;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public final class WidgetLibrary {
    public static Widget slider(String id, Component label, double min, double max, double value) {
        return new Widget("slider", id, label, null, List.of(min, max, value));
    }

    public static Widget textField(String id, Component label, String value) {
        return new Widget("text_field", id, label, null, List.of(value));
    }

    public static Widget dropdown(String id, Component label, List<String> values, String selected) {
        return new Widget("dropdown", id, label, null, List.of(values, selected));
    }

    public static Widget energyBar(String id, long amount, long capacity) {
        return new Widget("energy_bar", id, Component.empty(), null, List.of(amount, capacity));
    }

    public static Widget fluidTank(String id, ResourceLocation fluid, long amount, long capacity) {
        return new Widget("fluid_tank", id, Component.empty(), fluid, List.of(amount, capacity));
    }

    public static Widget progressArrow(String id, int progress, int maxProgress) {
        return new Widget("progress_arrow", id, Component.empty(), null, List.of(progress, maxProgress));
    }

    public record Widget(String type, String id, Component label, ResourceLocation texture, List<?> data) {
    }

    private WidgetLibrary() {
    }
}
