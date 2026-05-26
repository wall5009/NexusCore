package com.rollylindenshnizzer.nexuscore.fluid;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.material.Fluid;

public class FluidTank {
    private final long capacity;
    private FluidStack stored = FluidStack.EMPTY;

    public FluidTank(long capacity) {
        if (capacity < 0) {
            throw new IllegalArgumentException("Fluid capacity must be non-negative");
        }
        this.capacity = capacity;
    }

    public long capacity() {
        return capacity;
    }

    public FluidStack stored() {
        return stored;
    }

    public long remainingCapacity() {
        return capacity - stored.amount();
    }

    public boolean accepts(Fluid fluid) {
        return stored.isEmpty() || stored.fluid() == fluid;
    }

    public long fill(FluidStack stack, boolean simulate) {
        if (stack.isEmpty() || !accepts(stack.fluid())) {
            return 0;
        }
        long moved = Math.min(stack.amount(), remainingCapacity());
        if (!simulate && moved > 0) {
            stored = new FluidStack(stack.fluid(), stored.amount() + moved);
        }
        return moved;
    }

    public FluidStack drain(long requested, boolean simulate) {
        if (stored.isEmpty() || requested <= 0) {
            return FluidStack.EMPTY;
        }
        long moved = Math.min(requested, stored.amount());
        FluidStack result = new FluidStack(stored.fluid(), moved);
        if (!simulate) {
            stored = stored.amount() == moved ? FluidStack.EMPTY : stored.withAmount(stored.amount() - moved);
        }
        return result;
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putLong("Capacity", capacity);
        tag.put("Stored", stored.save());
        return tag;
    }

    public void load(CompoundTag tag) {
        stored = FluidStack.load(tag.getCompound("Stored"));
        if (stored.amount() > capacity) {
            stored = stored.withAmount(capacity);
        }
    }
}
