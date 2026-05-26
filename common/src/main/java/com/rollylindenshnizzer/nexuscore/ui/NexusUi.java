package com.rollylindenshnizzer.nexuscore.ui;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public final class NexusUi {
    public static ScreenSpec screen(Component title) {
        return new ScreenSpec(title);
    }

    public static WidgetSpec button(String id, Component label) {
        return new WidgetSpec("button", id, label, null, 0, 0, 0, 0);
    }

    public static WidgetSpec toggle(String id, Component label) {
        return new WidgetSpec("toggle", id, label, null, 0, 0, 0, 0);
    }

    public static WidgetSpec icon(String id, ResourceLocation texture) {
        return new WidgetSpec("icon", id, Component.empty(), texture, 0, 0, 0, 0);
    }

    public static WidgetSpec itemStack(String id, ItemStack stack) {
        return new WidgetSpec("item_stack", id, stack.getHoverName(), null, 0, 0, 18, 18);
    }

    private NexusUi() {
    }

    public static final class ScreenSpec {
        private final Component title;
        private final List<WidgetSpec> widgets = new ArrayList<>();

        private ScreenSpec(Component title) {
            this.title = title;
        }

        public ScreenSpec add(WidgetSpec widget) {
            widgets.add(widget);
            return this;
        }

        public Component title() {
            return title;
        }

        public List<WidgetSpec> widgets() {
            return List.copyOf(widgets);
        }
    }

    public record WidgetSpec(String type, String id, Component label, ResourceLocation texture, int x, int y, int width, int height) {
        public WidgetSpec bounds(int x, int y, int width, int height) {
            return new WidgetSpec(type, id, label, texture, x, y, width, height);
        }
    }
}
