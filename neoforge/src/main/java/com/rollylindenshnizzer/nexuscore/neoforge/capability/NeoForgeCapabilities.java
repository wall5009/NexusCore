package com.rollylindenshnizzer.nexuscore.neoforge.capability;

import com.rollylindenshnizzer.nexuscore.energy.EnergyStorage;
import com.rollylindenshnizzer.nexuscore.fluid.FluidTank;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

public final class NeoForgeCapabilities {
    public static IEnergyStorage energy(EnergyStorage storage) {
        return new NeoForgeEnergyStorageAdapter(storage);
    }

    public static IFluidHandler fluid(FluidTank tank) {
        return new NeoForgeFluidHandlerAdapter(tank);
    }

    private NeoForgeCapabilities() {
    }
}
