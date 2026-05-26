package com.rollylindenshnizzer.nexuscore.compat.recipeviewer;

import com.rollylindenshnizzer.nexuscore.fluid.FluidStack;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public record RecipeViewerPage(String name, List<RecipeViewerWidget> widgets,
                               List<RecipeViewerAdvancedControl> controls) {
    public RecipeViewerPage {
        name = name == null || name.isBlank() ? "main" : name;
        widgets = widgets == null ? List.of() : List.copyOf(widgets);
        controls = controls == null ? List.of() : List.copyOf(controls);
    }

    public List<RecipeViewerSlotWidget> slots() {
        return widgets.stream()
                .filter(RecipeViewerSlotWidget.class::isInstance)
                .map(RecipeViewerSlotWidget.class::cast)
                .toList();
    }

    public List<RecipeViewerProgressWidget> progressWidgets() {
        return widgets.stream()
                .filter(RecipeViewerProgressWidget.class::isInstance)
                .map(RecipeViewerProgressWidget.class::cast)
                .toList();
    }

    public List<RecipeViewerTextWidget> textWidgets() {
        return widgets.stream()
                .filter(RecipeViewerTextWidget.class::isInstance)
                .map(RecipeViewerTextWidget.class::cast)
                .toList();
    }

    public List<RecipeViewerTooltipWidget> tooltipWidgets() {
        return widgets.stream()
                .filter(RecipeViewerTooltipWidget.class::isInstance)
                .map(RecipeViewerTooltipWidget.class::cast)
                .toList();
    }

    public static Builder builder(String name) {
        return new Builder(name);
    }

    public static final class Builder {
        private final String name;
        private final List<RecipeViewerWidget> widgets = new ArrayList<>();
        private final List<RecipeViewerAdvancedControl> controls = new ArrayList<>();

        private Builder(String name) {
            this.name = name;
        }

        public Builder widget(RecipeViewerWidget widget) {
            if (widget != null) {
                widgets.add(widget);
            }
            return this;
        }

        public Builder slot(RecipeViewerRole role, int x, int y, Consumer<RecipeViewerSlotWidget.Builder> builder) {
            RecipeViewerSlotWidget.Builder slot = RecipeViewerSlotWidget.builder(role, x, y);
            builder.accept(slot);
            return widget(slot.build());
        }

        public Builder item(RecipeViewerRole role, int x, int y, ItemStack item) {
            return slot(role, x, y, slot -> slot.item(item));
        }

        public Builder itemInput(int x, int y, ItemStack item) {
            return item(RecipeViewerRole.INPUT, x, y, item);
        }

        public Builder itemOutput(int x, int y, ItemStack item) {
            return item(RecipeViewerRole.OUTPUT, x, y, item);
        }

        public Builder itemCatalyst(int x, int y, ItemStack item) {
            return item(RecipeViewerRole.CATALYST, x, y, item);
        }

        public Builder fluid(RecipeViewerRole role, int x, int y, int width, int height, long capacity,
                             FluidStack fluid) {
            return slot(role, x, y, slot -> slot.size(width, height).fluidCapacity(capacity).fluid(fluid));
        }

        public Builder fluidInput(int x, int y, int width, int height, long capacity, FluidStack fluid) {
            return fluid(RecipeViewerRole.INPUT, x, y, width, height, capacity, fluid);
        }

        public Builder fluidOutput(int x, int y, int width, int height, long capacity, FluidStack fluid) {
            return fluid(RecipeViewerRole.OUTPUT, x, y, width, height, capacity, fluid);
        }

        public Builder arrowProgress(int x, int y, int durationMillis) {
            return widget(RecipeViewerProgressWidget.arrow(x, y, durationMillis));
        }

        public Builder texturedProgress(ResourceLocation texture, int x, int y, int width, int height, int u, int v,
                                        int durationMillis) {
            return widget(RecipeViewerProgressWidget.texture(texture, x, y, width, height, u, v, durationMillis));
        }

        public Builder progress(RecipeViewerProgressWidget progress) {
            return widget(progress);
        }

        public Builder text(Component text, int x, int y) {
            return text(text, x, y, 0x404040, false);
        }

        public Builder text(Component text, int x, int y, int color, boolean shadow) {
            return widget(new RecipeViewerTextWidget(text, x, y, color, shadow));
        }

        public Builder tooltip(int x, int y, int width, int height, List<Component> lines) {
            return widget(new RecipeViewerTooltipWidget(x, y, width, height, lines));
        }

        public Builder control(RecipeViewerAdvancedControl control) {
            if (control != null) {
                controls.add(control);
            }
            return this;
        }

        public Builder viewerControl(String viewer, String type, Map<String, String> properties) {
            return control(new RecipeViewerAdvancedControl(viewer, type, properties));
        }

        public Builder jeiTransferButton(int x, int y) {
            return viewerControl("jei", "recipe_transfer_button", Map.of("x", Integer.toString(x), "y", Integer.toString(y)));
        }

        public Builder viewerTooltip(String viewer, int x, int y, int width, int height, Component line) {
            return viewerControl(viewer, "tooltip", Map.of(
                    "x", Integer.toString(x),
                    "y", Integer.toString(y),
                    "width", Integer.toString(width),
                    "height", Integer.toString(height),
                    "text", line.getString()));
        }

        public RecipeViewerPage build() {
            return new RecipeViewerPage(name, widgets, controls);
        }
    }
}
