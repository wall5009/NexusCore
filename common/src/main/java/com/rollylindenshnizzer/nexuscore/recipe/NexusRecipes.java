package com.rollylindenshnizzer.nexuscore.recipe;

import com.rollylindenshnizzer.nexuscore.core.NexusIds;
import com.rollylindenshnizzer.nexuscore.registry.NexusRegistries;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;

public final class NexusRecipes {
    @SuppressWarnings("unchecked")
    public static <R extends Recipe<?>> RegistrySupplier<RecipeType<R>> type(String modId, String path) {
        String normalized = NexusIds.normalizePath(path);
        return (RegistrySupplier<RecipeType<R>>) (RegistrySupplier<?>) NexusRegistries.group(modId).recipeTypes()
                .register(normalized, () -> new RecipeType<R>() {
                    @Override
                    public String toString() {
                        return modId + ":" + normalized;
                    }
                });
    }

    @SuppressWarnings("unchecked")
    public static <S extends RecipeSerializer<?>> RegistrySupplier<S> serializer(String modId, String path, Supplier<S> supplier) {
        return (RegistrySupplier<S>) NexusRegistries.group(modId).recipeSerializers().register(path, supplier);
    }

    public static Optional<ItemStack> firstOutput(Collection<? extends Recipe<?>> recipes, Predicate<ItemStack> predicate) {
        return recipes.stream()
                .map(recipe -> recipe.getResultItem(null))
                .filter(predicate)
                .findFirst();
    }

    private NexusRecipes() {
    }
}
