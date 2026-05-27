package com.rollylindenshnizzer.nexuscore.machine;

import com.rollylindenshnizzer.nexuscore.api.NexusStable;
import com.rollylindenshnizzer.nexuscore.energy.NexusEnergyStorage;
import com.rollylindenshnizzer.nexuscore.fluid.FluidStack;
import com.rollylindenshnizzer.nexuscore.fluid.NexusFluidTank;
import com.rollylindenshnizzer.nexuscore.inventory.InventoryTransfer;
import com.rollylindenshnizzer.nexuscore.inventory.SimpleItemHandler;
import com.rollylindenshnizzer.nexuscore.inventory.SlotRange;
import com.rollylindenshnizzer.nexuscore.inventory.SlotRole;
import net.minecraft.world.item.ItemStack;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;

@NexusStable(since = "1.2")
public final class MachineProcessingEngine {
    private final NexusMachineDefinition definition;
    private final SimpleItemHandler inventory;
    private final NexusEnergyStorage energy;
    private final NexusFluidTank fluid;
    private final MachineState state;
    private final Random random;
    private MachineRecipeDefinition activeRecipe;
    private MachineRecipeDefinition lockedRecipe;
    private long remainingEnergyCost;

    public MachineProcessingEngine(NexusMachineDefinition definition,
                                   SimpleItemHandler inventory,
                                   NexusEnergyStorage energy,
                                   NexusFluidTank fluid,
                                   MachineState state) {
        this(definition, inventory, energy, fluid, state, new Random());
    }

    public MachineProcessingEngine(NexusMachineDefinition definition,
                                   SimpleItemHandler inventory,
                                   NexusEnergyStorage energy,
                                   NexusFluidTank fluid,
                                   MachineState state,
                                   Random random) {
        this.definition = Objects.requireNonNull(definition, "definition");
        this.inventory = Objects.requireNonNull(inventory, "inventory");
        this.energy = Objects.requireNonNull(energy, "energy");
        this.fluid = Objects.requireNonNull(fluid, "fluid");
        this.state = Objects.requireNonNull(state, "state");
        this.random = Objects.requireNonNull(random, "random");
        this.state.redstoneMode(definition.defaultRedstoneMode());
    }

    public MachineProcessResult tick(List<MachineRecipeDefinition> recipes, boolean powered) {
        if (!state.redstoneAllows(powered)) {
            state.status(MachineState.Status.BLOCKED);
            return MachineProcessResult.stalled(MachineStallReason.REDSTONE_BLOCKED, "redstone mode " + state.redstoneMode());
        }
        if (activeRecipe == null) {
            Optional<MachineRecipeDefinition> recipe = findRecipe(recipes);
            if (recipe.isEmpty()) {
                state.status(MachineState.Status.IDLE);
                return MachineProcessResult.stalled(MachineStallReason.NO_RECIPE, "no matching recipe");
            }
            start(recipe.get());
        }

        long energyThisTick = energyThisTick(activeRecipe);
        if (energyThisTick > 0 && energy.extract(energyThisTick, true) < energyThisTick) {
            state.status(MachineState.Status.BLOCKED);
            return MachineProcessResult.stalled(MachineStallReason.NOT_ENOUGH_ENERGY, "need " + energyThisTick + " energy this tick");
        }
        if (energyThisTick > 0) {
            energy.extract(energyThisTick, false);
            remainingEnergyCost -= energyThisTick;
        }

        state.status(MachineState.Status.RUNNING);
        if (state.tickProgress()) {
            MachineProcessResult result = complete(activeRecipe);
            activeRecipe = null;
            remainingEnergyCost = 0;
            return result;
        }
        return MachineProcessResult.progress();
    }

    public void lockRecipe(MachineRecipeDefinition recipe) {
        lockedRecipe = recipe;
    }

    public void clearRecipeLock() {
        lockedRecipe = null;
    }

    public MachineRecipeDefinition activeRecipe() {
        return activeRecipe;
    }

    public boolean matches(MachineRecipeDefinition recipe) {
        return hasItemInputs(recipe) && hasFluidInputs(recipe) && hasOutputSpace(recipe);
    }

    private Optional<MachineRecipeDefinition> findRecipe(List<MachineRecipeDefinition> recipes) {
        if (lockedRecipe != null && matches(lockedRecipe)) {
            return Optional.of(lockedRecipe);
        }
        return recipes.stream()
                .filter(this::matches)
                .max(Comparator.comparingInt(MachineRecipeDefinition::priority));
    }

    private void start(MachineRecipeDefinition recipe) {
        activeRecipe = recipe;
        remainingEnergyCost = recipe.energyCost();
        state.start(recipe.processingTicks());
    }

    private MachineProcessResult complete(MachineRecipeDefinition recipe) {
        if (!hasItemInputs(recipe)) {
            return MachineProcessResult.stalled(MachineStallReason.MISSING_ITEM_INPUT, recipe.id().toString());
        }
        if (!hasFluidInputs(recipe)) {
            return MachineProcessResult.stalled(MachineStallReason.MISSING_FLUID_INPUT, recipe.id().toString());
        }
        if (!hasOutputSpace(recipe)) {
            return MachineProcessResult.stalled(MachineStallReason.OUTPUT_BLOCKED, recipe.id().toString());
        }

        consumeItems(recipe.itemInputs());
        for (FluidStack input : recipe.fluidInputs()) {
            fluid.drain(input.amount(), false);
        }
        insertOutputs(recipe.itemOutputs());
        for (MachineRecipeDefinition.ChanceOutput output : recipe.chanceOutputs()) {
            if (random.nextDouble() <= output.chance()) {
                insertOutputs(List.of(output.stack()));
            }
        }
        for (FluidStack output : recipe.fluidOutputs()) {
            fluid.fill(output, false);
        }
        state.status(MachineState.Status.IDLE);
        return MachineProcessResult.complete();
    }

    private long energyThisTick(MachineRecipeDefinition recipe) {
        if (remainingEnergyCost <= 0) {
            return 0;
        }
        long perTick = Math.max(1, (long) Math.ceil(recipe.energyCost() / (double) recipe.processingTicks()));
        return Math.min(remainingEnergyCost, perTick);
    }

    private boolean hasItemInputs(MachineRecipeDefinition recipe) {
        for (ItemStack needed : recipe.itemInputs()) {
            int remaining = needed.getCount();
            for (int slot : definition.slotsWithRole(SlotRole.INPUT)) {
                ItemStack stack = inventory.get(slot);
                if (ItemStack.isSameItemSameComponents(stack, needed)) {
                    remaining -= stack.getCount();
                    if (remaining <= 0) {
                        break;
                    }
                }
            }
            if (remaining > 0) {
                return false;
            }
        }
        return true;
    }

    private boolean hasFluidInputs(MachineRecipeDefinition recipe) {
        for (FluidStack input : recipe.fluidInputs()) {
            FluidStack stored = fluid.stored();
            if (stored.isEmpty() || !stored.sameFluid(input) || stored.amount() < input.amount()) {
                return false;
            }
        }
        return true;
    }

    private boolean hasOutputSpace(MachineRecipeDefinition recipe) {
        SlotRange outputRange = outputRange();
        for (ItemStack output : recipe.itemOutputs()) {
            ItemStack remainder = InventoryTransfer.insertInto(inventory, outputRange, output, true,
                    new java.util.ArrayList<>(), "machine-output-preview");
            if (!remainder.isEmpty()) {
                return false;
            }
        }
        for (MachineRecipeDefinition.ChanceOutput output : recipe.chanceOutputs()) {
            ItemStack remainder = InventoryTransfer.insertInto(inventory, outputRange, output.stack(), true,
                    new java.util.ArrayList<>(), "machine-chance-output-preview");
            if (!remainder.isEmpty()) {
                return false;
            }
        }
        for (FluidStack output : recipe.fluidOutputs()) {
            if (fluid.fill(output, true) < output.amount()) {
                return false;
            }
        }
        return true;
    }

    private void consumeItems(List<ItemStack> inputs) {
        for (ItemStack needed : inputs) {
            int remaining = needed.getCount();
            for (int slot : definition.slotsWithRole(SlotRole.INPUT)) {
                ItemStack stack = inventory.get(slot);
                if (!ItemStack.isSameItemSameComponents(stack, needed)) {
                    continue;
                }
                ItemStack extracted = inventory.extract(slot, remaining, false);
                remaining -= extracted.getCount();
                if (remaining <= 0) {
                    break;
                }
            }
        }
    }

    private void insertOutputs(List<ItemStack> outputs) {
        SlotRange range = outputRange();
        for (ItemStack output : outputs) {
            InventoryTransfer.insertInto(inventory, range, output.copy(), false, new java.util.ArrayList<>(), "machine-output");
        }
    }

    private SlotRange outputRange() {
        List<Integer> slots = definition.slotsWithRole(SlotRole.OUTPUT);
        if (slots.isEmpty()) {
            return new SlotRange(0, inventory.size());
        }
        int first = slots.stream().min(Integer::compareTo).orElse(0);
        int last = slots.stream().max(Integer::compareTo).orElse(first) + 1;
        return new SlotRange(first, last);
    }
}
