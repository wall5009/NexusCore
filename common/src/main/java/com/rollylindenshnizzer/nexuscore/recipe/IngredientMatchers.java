package com.rollylindenshnizzer.nexuscore.recipe;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.List;

public final class IngredientMatchers {
    public static boolean matchesAll(List<Ingredient> ingredients, List<ItemStack> inputs) {
        if (ingredients.size() > inputs.size()) {
            return false;
        }
        for (int i = 0; i < ingredients.size(); i++) {
            if (!ingredients.get(i).test(inputs.get(i))) {
                return false;
            }
        }
        return true;
    }

    public static boolean sameItemAndComponents(ItemStack left, ItemStack right) {
        return ItemStack.isSameItemSameComponents(left, right);
    }

    private IngredientMatchers() {
    }
}
