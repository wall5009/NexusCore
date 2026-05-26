package com.rollylindenshnizzer.nexuscore.compat.recipeviewer;

import com.rollylindenshnizzer.nexuscore.fluid.FluidStack;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public record RecipeViewerSlotWidget(RecipeViewerRole role,
                                     int x,
                                     int y,
                                     int width,
                                     int height,
                                     long fluidCapacity,
                                     boolean drawBackground,
                                     boolean large,
                                     String name,
                                     List<ItemStack> items,
                                     List<FluidStack> fluids,
                                     List<Component> tooltip) implements RecipeViewerWidget {
    public RecipeViewerSlotWidget {
        role = role == null ? RecipeViewerRole.RENDER_ONLY : role;
        width = Math.max(1, width);
        height = Math.max(1, height);
        fluidCapacity = Math.max(1, fluidCapacity);
        name = name == null || name.isBlank() ? "" : name;
        items = items == null ? List.of() : items.stream()
                .filter(stack -> stack != null && !stack.isEmpty())
                .map(ItemStack::copy)
                .toList();
        fluids = fluids == null ? List.of() : fluids.stream()
                .filter(stack -> stack != null && !stack.isEmpty())
                .toList();
        tooltip = tooltip == null ? List.of() : List.copyOf(tooltip);
    }

    public boolean hasItems() {
        return !items.isEmpty();
    }

    public boolean hasFluids() {
        return !fluids.isEmpty();
    }

    public static Builder builder(RecipeViewerRole role, int x, int y) {
        return new Builder(role, x, y);
    }

    public static final class Builder {
        private final RecipeViewerRole role;
        private final int x;
        private final int y;
        private int width = 18;
        private int height = 18;
        private long fluidCapacity = 1_000;
        private boolean drawBackground = true;
        private boolean large;
        private String name = "";
        private final List<ItemStack> items = new ArrayList<>();
        private final List<FluidStack> fluids = new ArrayList<>();
        private final List<Component> tooltip = new ArrayList<>();

        private Builder(RecipeViewerRole role, int x, int y) {
            this.role = role;
            this.x = x;
            this.y = y;
        }

        public Builder size(int width, int height) {
            this.width = width;
            this.height = height;
            return this;
        }

        public Builder fluidCapacity(long fluidCapacity) {
            this.fluidCapacity = fluidCapacity;
            return this;
        }

        public Builder background(boolean drawBackground) {
            this.drawBackground = drawBackground;
            return this;
        }

        public Builder large(boolean large) {
            this.large = large;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder item(ItemStack item) {
            if (item != null && !item.isEmpty()) {
                this.items.add(item.copy());
            }
            return this;
        }

        public Builder items(Iterable<ItemStack> items) {
            if (items != null) {
                for (ItemStack item : items) {
                    item(item);
                }
            }
            return this;
        }

        public Builder fluid(FluidStack fluid) {
            if (fluid != null && !fluid.isEmpty()) {
                this.fluids.add(fluid);
            }
            return this;
        }

        public Builder fluids(Iterable<FluidStack> fluids) {
            if (fluids != null) {
                for (FluidStack fluid : fluids) {
                    fluid(fluid);
                }
            }
            return this;
        }

        public Builder tooltip(Component line) {
            if (line != null) {
                this.tooltip.add(line);
            }
            return this;
        }

        public RecipeViewerSlotWidget build() {
            return new RecipeViewerSlotWidget(role, x, y, width, height, fluidCapacity, drawBackground, large, name,
                    items, fluids, tooltip);
        }
    }
}
