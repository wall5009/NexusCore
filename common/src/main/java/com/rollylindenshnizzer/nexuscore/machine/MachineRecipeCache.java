package com.rollylindenshnizzer.nexuscore.machine;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class MachineRecipeCache {
    private final Map<ResourceLocation, MachineRecipe> byId = new HashMap<>();

    public void replaceAll(Collection<MachineRecipe> recipes) {
        byId.clear();
        for (MachineRecipe recipe : recipes) {
            byId.put(recipe.id(), recipe);
        }
    }

    public Optional<MachineRecipe> byId(ResourceLocation id) {
        return Optional.ofNullable(byId.get(id));
    }

    public Optional<MachineRecipe> firstMatchingInput(ItemStack stack) {
        return byId.values().stream()
                .filter(recipe -> recipe.itemInputs().stream().anyMatch(input -> ItemStack.isSameItemSameComponents(input, stack)))
                .findFirst();
    }
}
