package com.rollylindenshnizzer.nexuscore.compat.recipeviewer;

import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public record RecipeViewerLayout(int width, int height, List<RecipeViewerPage> pages,
                                 List<RecipeViewerAdvancedControl> controls) {
    public RecipeViewerLayout {
        width = Math.max(1, width);
        height = Math.max(1, height);
        pages = pages == null || pages.isEmpty() ? List.of(new RecipeViewerPage("main", List.of(), List.of())) : List.copyOf(pages);
        controls = controls == null ? List.of() : List.copyOf(controls);
    }

    public List<RecipeViewerAdvancedControl> controlsFor(RecipeViewerPage page) {
        List<RecipeViewerAdvancedControl> combined = new ArrayList<>(controls);
        combined.addAll(page.controls());
        return List.copyOf(combined);
    }

    public static Builder builder(int width, int height) {
        return new Builder(width, height);
    }

    public static RecipeViewerLayout itemStackLayout(int width, int height, List<ItemStack> inputs,
                                                     List<ItemStack> outputs, List<ItemStack> catalysts) {
        List<ItemStack> safeInputs = inputs == null ? List.of() : inputs;
        List<ItemStack> safeOutputs = outputs == null ? List.of() : outputs;
        List<ItemStack> safeCatalysts = catalysts == null ? List.of() : catalysts;
        return builder(width, height)
                .page("main", page -> {
                    for (int index = 0; index < safeInputs.size(); index++) {
                        page.itemInput(index * 18, 0, safeInputs.get(index));
                    }
                    int outputX = Math.max(0, width - Math.max(1, safeOutputs.size()) * 18);
                    for (int index = 0; index < safeOutputs.size(); index++) {
                        page.itemOutput(outputX + index * 18, 0, safeOutputs.get(index));
                    }
                    int catalystY = Math.max(18, height - 18);
                    for (int index = 0; index < safeCatalysts.size(); index++) {
                        page.itemCatalyst(index * 18, catalystY, safeCatalysts.get(index));
                    }
                })
                .build();
    }

    public static final class Builder {
        private final int width;
        private final int height;
        private final List<RecipeViewerPage> pages = new ArrayList<>();
        private final List<RecipeViewerAdvancedControl> controls = new ArrayList<>();

        private Builder(int width, int height) {
            this.width = width;
            this.height = height;
        }

        public Builder page(String name, Consumer<RecipeViewerPage.Builder> builder) {
            RecipeViewerPage.Builder page = RecipeViewerPage.builder(name);
            builder.accept(page);
            pages.add(page.build());
            return this;
        }

        public Builder page(Consumer<RecipeViewerPage.Builder> builder) {
            return page("main", builder);
        }

        public Builder control(RecipeViewerAdvancedControl control) {
            if (control != null) {
                controls.add(control);
            }
            return this;
        }

        public RecipeViewerLayout build() {
            return new RecipeViewerLayout(width, height, pages, controls);
        }
    }
}
