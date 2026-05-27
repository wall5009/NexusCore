package com.rollylindenshnizzer.nexuscore.machine;

import com.rollylindenshnizzer.nexuscore.api.NexusStable;
import net.minecraft.resources.ResourceLocation;

@NexusStable(since = "1.2")
public record MachineUpgrade(ResourceLocation id,
                             double speedMultiplier,
                             double energyMultiplier,
                             int extraItemSlots,
                             long extraEnergyCapacity,
                             long extraFluidCapacity) {
    public MachineUpgrade {
        if (speedMultiplier <= 0 || energyMultiplier <= 0) {
            throw new IllegalArgumentException("Machine upgrade multipliers must be positive");
        }
        extraItemSlots = Math.max(0, extraItemSlots);
        extraEnergyCapacity = Math.max(0, extraEnergyCapacity);
        extraFluidCapacity = Math.max(0, extraFluidCapacity);
    }

    public static MachineUpgrade speed(ResourceLocation id, double multiplier, double energyMultiplier) {
        return new MachineUpgrade(id, multiplier, energyMultiplier, 0, 0, 0);
    }

    public static MachineUpgrade capacity(ResourceLocation id, int itemSlots, long energy, long fluid) {
        return new MachineUpgrade(id, 1.0, 1.0, itemSlots, energy, fluid);
    }
}
