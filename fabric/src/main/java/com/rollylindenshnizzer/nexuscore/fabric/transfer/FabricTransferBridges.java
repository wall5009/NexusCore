package com.rollylindenshnizzer.nexuscore.fabric.transfer;

import com.rollylindenshnizzer.nexuscore.energy.EnergyStorage;
import com.rollylindenshnizzer.nexuscore.fluid.FluidTank;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;

public final class FabricTransferBridges {
    public static team.reborn.energy.api.EnergyStorage energy(EnergyStorage storage) {
        return new FabricEnergyStorageAdapter(storage);
    }

    public static Storage<FluidVariant> fluid(FluidTank tank) {
        return new FabricFluidStorageAdapter(tank);
    }

    private FabricTransferBridges() {
    }
}
