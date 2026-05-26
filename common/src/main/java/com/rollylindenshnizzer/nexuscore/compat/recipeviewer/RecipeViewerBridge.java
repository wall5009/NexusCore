package com.rollylindenshnizzer.nexuscore.compat.recipeviewer;

import com.rollylindenshnizzer.nexuscore.compat.IntegrationDescriptor;

import java.util.ArrayList;
import java.util.List;

public final class RecipeViewerBridge {
    private static final List<RecipeViewerCategory> CATEGORIES = new ArrayList<>();
    private static final List<RecipeViewerDisplay> DISPLAYS = new ArrayList<>();
    private static final List<IntegrationDescriptor> VIEWERS = new ArrayList<>();

    static {
        VIEWERS.add(new IntegrationDescriptor("emi", List.of("emi"), "EMI recipe display bridge"));
        VIEWERS.add(new IntegrationDescriptor("rei", List.of("roughlyenoughitems"), "REI recipe display bridge"));
        VIEWERS.add(new IntegrationDescriptor("jei", List.of("jei"), "JEI recipe display bridge"));
    }

    public static void category(RecipeViewerCategory category) {
        CATEGORIES.add(category);
    }

    public static void display(RecipeViewerDisplay display) {
        DISPLAYS.add(display);
    }

    public static List<RecipeViewerCategory> categories() {
        return List.copyOf(CATEGORIES);
    }

    public static List<RecipeViewerDisplay> displays() {
        return List.copyOf(DISPLAYS);
    }

    public static List<RecipeViewerDisplay> displaysFor(RecipeViewerCategory category) {
        return displaysFor(category.id());
    }

    public static List<RecipeViewerDisplay> displaysFor(net.minecraft.resources.ResourceLocation categoryId) {
        return DISPLAYS.stream().filter(display -> display.categoryId().equals(categoryId)).toList();
    }

    public static List<RecipeViewerDisplayPage> displayPagesFor(RecipeViewerCategory category) {
        return displaysFor(category).stream()
                .flatMap(display -> {
                    RecipeViewerLayout layout = display.layoutOrDefault(category);
                    return java.util.stream.IntStream.range(0, layout.pages().size())
                            .mapToObj(index -> new RecipeViewerDisplayPage(category, display, layout,
                                    layout.pages().get(index), index));
                })
                .toList();
    }

    public static List<IntegrationDescriptor> availableViewers() {
        return VIEWERS.stream().filter(IntegrationDescriptor::available).toList();
    }

    private RecipeViewerBridge() {
    }
}
