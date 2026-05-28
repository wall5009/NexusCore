package com.rollylindenshnizzer.nexuscore.multiblock;

import com.rollylindenshnizzer.nexuscore.api.NexusIncubating;

import java.util.List;

@NexusIncubating(since = "1.3")
public final class NexusMultiblockPredicates {
    public static NexusMultiblocks.MultiblockPredicate hasEnergyStorage() {
        return new NexusMultiblocks.MultiblockPredicate("has energy storage capability", false,
                List.of("Use an energy port role when this part should aggregate into the controller buffer."));
    }

    public static NexusMultiblocks.MultiblockPredicate hasFluidTank() {
        return new NexusMultiblocks.MultiblockPredicate("has fluid tank capability", false,
                List.of("Use a fluid port role when this part should aggregate tanks."));
    }

    public static NexusMultiblocks.MultiblockPredicate hasInventory() {
        return new NexusMultiblocks.MultiblockPredicate("has item inventory capability", false,
                List.of("Use input or output hatch roles to make routing clear."));
    }

    public static NexusMultiblocks.BlockMatcher itemPort() {
        return NexusMultiblocks.BlockMatcher.predicate('?', hasInventory(), false)
                .withRole(NexusMultiblocks.PartRole.ITEM_PORT);
    }

    public static NexusMultiblocks.BlockMatcher energyPort() {
        return NexusMultiblocks.BlockMatcher.predicate('?', hasEnergyStorage(), false)
                .withRole(NexusMultiblocks.PartRole.ENERGY_PORT);
    }

    public static NexusMultiblocks.BlockMatcher fluidPort() {
        return NexusMultiblocks.BlockMatcher.predicate('?', hasFluidTank(), false)
                .withRole(NexusMultiblocks.PartRole.FLUID_PORT);
    }

    public static NexusMultiblocks.MultiblockPredicate expensiveWorldScan(String description) {
        return new NexusMultiblocks.MultiblockPredicate(description, true,
                List.of("Cache this predicate or restrict revalidation to affected regions."));
    }

    private NexusMultiblockPredicates() {
    }
}
