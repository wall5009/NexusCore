package com.rollylindenshnizzer.nexuscore.compat.recipeviewer;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public record RecipeViewerCategory(ResourceLocation id, Component title, ItemStack icon, int width, int height,
                                   List<ItemStack> workstations) {
    public RecipeViewerCategory {
        workstations = List.copyOf(workstations);
    }

    public RecipeViewerCategory(ResourceLocation id, Component title, ItemStack icon, int width, int height) {
        this(id, title, icon, width, height, List.of());
    }

    public RecipeViewerCategory(ResourceLocation id, Component title, ItemStack icon, int width, int height,
                                ItemStack workstation) {
        this(id, title, icon, width, height, List.of(workstation));
    }
}
