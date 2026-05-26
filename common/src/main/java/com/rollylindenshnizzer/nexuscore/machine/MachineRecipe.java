package com.rollylindenshnizzer.nexuscore.machine;

import com.rollylindenshnizzer.nexuscore.fluid.FluidStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public record MachineRecipe(ResourceLocation id, List<ItemStack> itemInputs, List<ItemStack> itemOutputs,
                            List<FluidStack> fluidInputs, List<FluidStack> fluidOutputs,
                            long energyCost, int processingTicks) {
    public MachineRecipe {
        itemInputs = List.copyOf(itemInputs);
        itemOutputs = List.copyOf(itemOutputs);
        fluidInputs = List.copyOf(fluidInputs);
        fluidOutputs = List.copyOf(fluidOutputs);
        if (energyCost < 0 || processingTicks <= 0) {
            throw new IllegalArgumentException("Machine recipes need non-negative energy and positive processing time");
        }
    }
}
