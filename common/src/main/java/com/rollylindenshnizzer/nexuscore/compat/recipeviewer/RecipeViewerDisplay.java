package com.rollylindenshnizzer.nexuscore.compat.recipeviewer;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

public final class RecipeViewerDisplay {
    private final ResourceLocation id;
    private final ResourceLocation categoryId;
    private final List<ItemStack> inputs;
    private final List<ItemStack> outputs;
    private final List<ItemStack> catalysts;
    private final RecipeViewerLayout layout;

    public RecipeViewerDisplay(ResourceLocation id, ResourceLocation categoryId, List<ItemStack> inputs,
                               List<ItemStack> outputs) {
        this(id, categoryId, inputs, outputs, List.of());
    }

    public RecipeViewerDisplay(ResourceLocation id, ResourceLocation categoryId, List<ItemStack> inputs,
                               List<ItemStack> outputs, List<ItemStack> catalysts) {
        this(id, categoryId, inputs, outputs, catalysts, null);
    }

    public RecipeViewerDisplay(ResourceLocation id, ResourceLocation categoryId, RecipeViewerLayout layout) {
        this(id, categoryId, List.of(), List.of(), List.of(), layout);
    }

    public RecipeViewerDisplay(ResourceLocation id, ResourceLocation categoryId, List<ItemStack> inputs,
                               List<ItemStack> outputs, List<ItemStack> catalysts, RecipeViewerLayout layout) {
        this.id = Objects.requireNonNull(id, "id");
        this.categoryId = Objects.requireNonNull(categoryId, "categoryId");
        this.inputs = copyStacks(inputs);
        this.outputs = copyStacks(outputs);
        this.catalysts = copyStacks(catalysts);
        this.layout = layout;
    }

    public static Builder builder(ResourceLocation id, ResourceLocation categoryId, int width, int height) {
        return new Builder(id, categoryId, width, height);
    }

    public ResourceLocation id() {
        return id;
    }

    public ResourceLocation categoryId() {
        return categoryId;
    }

    public List<ItemStack> inputs() {
        return inputs;
    }

    public List<ItemStack> outputs() {
        return outputs;
    }

    public List<ItemStack> catalysts() {
        return catalysts;
    }

    public RecipeViewerLayout layout() {
        return layout == null ? RecipeViewerLayout.itemStackLayout(116, 54, inputs, outputs, catalysts) : layout;
    }

    public Optional<RecipeViewerLayout> customLayout() {
        return Optional.ofNullable(layout);
    }

    public boolean hasCustomLayout() {
        return layout != null;
    }

    public RecipeViewerLayout layoutOrDefault(RecipeViewerCategory category) {
        return layout != null
                ? layout
                : RecipeViewerLayout.itemStackLayout(category.width(), category.height(), inputs, outputs, catalysts);
    }

    private static List<ItemStack> copyStacks(List<ItemStack> stacks) {
        if (stacks == null) {
            return List.of();
        }
        return stacks.stream()
                .filter(stack -> stack != null && !stack.isEmpty())
                .map(ItemStack::copy)
                .toList();
    }

    public static final class Builder {
        private final ResourceLocation id;
        private final ResourceLocation categoryId;
        private final RecipeViewerLayout.Builder layout;

        private Builder(ResourceLocation id, ResourceLocation categoryId, int width, int height) {
            this.id = id;
            this.categoryId = categoryId;
            this.layout = RecipeViewerLayout.builder(width, height);
        }

        public Builder page(String name, Consumer<RecipeViewerPage.Builder> builder) {
            layout.page(name, builder);
            return this;
        }

        public Builder page(Consumer<RecipeViewerPage.Builder> builder) {
            layout.page(builder);
            return this;
        }

        public Builder control(RecipeViewerAdvancedControl control) {
            layout.control(control);
            return this;
        }

        public RecipeViewerDisplay build() {
            return new RecipeViewerDisplay(id, categoryId, layout.build());
        }
    }
}
