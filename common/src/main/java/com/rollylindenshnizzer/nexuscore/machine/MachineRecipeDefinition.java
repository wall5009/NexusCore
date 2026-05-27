package com.rollylindenshnizzer.nexuscore.machine;

import com.rollylindenshnizzer.nexuscore.api.NexusStable;
import com.rollylindenshnizzer.nexuscore.fluid.FluidStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@NexusStable(since = "1.2")
public record MachineRecipeDefinition(ResourceLocation id,
                                      ResourceLocation type,
                                      List<ItemStack> itemInputs,
                                      List<ItemStack> itemOutputs,
                                      List<FluidStack> fluidInputs,
                                      List<FluidStack> fluidOutputs,
                                      List<ChanceOutput> chanceOutputs,
                                      long energyCost,
                                      int processingTicks,
                                      int priority,
                                      String group,
                                      String category,
                                      Map<String, String> conditions) {
    public MachineRecipeDefinition {
        itemInputs = copyItems(itemInputs);
        itemOutputs = copyItems(itemOutputs);
        fluidInputs = fluidInputs == null ? List.of() : List.copyOf(fluidInputs);
        fluidOutputs = fluidOutputs == null ? List.of() : List.copyOf(fluidOutputs);
        chanceOutputs = chanceOutputs == null ? List.of() : List.copyOf(chanceOutputs);
        energyCost = Math.max(0, energyCost);
        processingTicks = Math.max(1, processingTicks);
        conditions = conditions == null ? Map.of() : Map.copyOf(conditions);
        group = group == null ? "" : group;
        category = category == null ? "" : category;
    }

    public static Builder builder(ResourceLocation id, ResourceLocation type) {
        return new Builder(id, type);
    }

    public MachineRecipe toSimpleRecipe() {
        return new MachineRecipe(id, itemInputs, itemOutputs, fluidInputs, fluidOutputs, energyCost, processingTicks);
    }

    private static List<ItemStack> copyItems(List<ItemStack> stacks) {
        if (stacks == null) {
            return List.of();
        }
        return stacks.stream().map(ItemStack::copy).toList();
    }

    public record ChanceOutput(ItemStack stack, double chance) {
        public ChanceOutput {
            stack = stack == null ? ItemStack.EMPTY : stack.copy();
            chance = Math.max(0.0, Math.min(1.0, chance));
        }
    }

    public static final class Builder {
        private final ResourceLocation id;
        private final ResourceLocation type;
        private final List<ItemStack> itemInputs = new ArrayList<>();
        private final List<ItemStack> itemOutputs = new ArrayList<>();
        private final List<FluidStack> fluidInputs = new ArrayList<>();
        private final List<FluidStack> fluidOutputs = new ArrayList<>();
        private final List<ChanceOutput> chanceOutputs = new ArrayList<>();
        private long energyCost;
        private int processingTicks = 100;
        private int priority;
        private String group = "";
        private String category = "";
        private Map<String, String> conditions = Map.of();

        private Builder(ResourceLocation id, ResourceLocation type) {
            this.id = id;
            this.type = type;
        }

        public Builder input(ItemStack stack) {
            itemInputs.add(stack.copy());
            return this;
        }

        public Builder output(ItemStack stack) {
            itemOutputs.add(stack.copy());
            return this;
        }

        public Builder fluidInput(FluidStack stack) {
            fluidInputs.add(stack);
            return this;
        }

        public Builder fluidOutput(FluidStack stack) {
            fluidOutputs.add(stack);
            return this;
        }

        public Builder chanceOutput(ItemStack stack, double chance) {
            chanceOutputs.add(new ChanceOutput(stack, chance));
            return this;
        }

        public Builder energy(long energyCost) {
            this.energyCost = energyCost;
            return this;
        }

        public Builder ticks(int processingTicks) {
            this.processingTicks = processingTicks;
            return this;
        }

        public Builder priority(int priority) {
            this.priority = priority;
            return this;
        }

        public Builder group(String group) {
            this.group = group;
            return this;
        }

        public Builder category(String category) {
            this.category = category;
            return this;
        }

        public Builder conditions(Map<String, String> conditions) {
            this.conditions = conditions;
            return this;
        }

        public MachineRecipeDefinition build() {
            return new MachineRecipeDefinition(id, type, itemInputs, itemOutputs, fluidInputs, fluidOutputs,
                    chanceOutputs, energyCost, processingTicks, priority, group, category, conditions);
        }
    }
}
