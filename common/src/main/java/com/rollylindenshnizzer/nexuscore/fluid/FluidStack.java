package com.rollylindenshnizzer.nexuscore.fluid;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

public record FluidStack(Fluid fluid, long amount) {
    public static final FluidStack EMPTY = new FluidStack(Fluids.EMPTY, 0);

    public FluidStack {
        if (amount < 0) {
            throw new IllegalArgumentException("Fluid amount must be non-negative");
        }
    }

    public boolean isEmpty() {
        return fluid == Fluids.EMPTY || amount <= 0;
    }

    public FluidStack withAmount(long newAmount) {
        return new FluidStack(fluid, newAmount);
    }

    public boolean sameFluid(FluidStack other) {
        return fluid == other.fluid;
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        if (!isEmpty()) {
            tag.putString("Fluid", BuiltInRegistries.FLUID.getKey(fluid).toString());
            tag.putLong("Amount", amount);
        }
        return tag;
    }

    public static FluidStack load(CompoundTag tag) {
        if (!tag.contains("Fluid")) {
            return EMPTY;
        }
        ResourceLocation id = ResourceLocation.tryParse(tag.getString("Fluid"));
        if (id == null || !BuiltInRegistries.FLUID.containsKey(id)) {
            return EMPTY;
        }
        return new FluidStack(BuiltInRegistries.FLUID.get(id), Math.max(0, tag.getLong("Amount")));
    }
}
