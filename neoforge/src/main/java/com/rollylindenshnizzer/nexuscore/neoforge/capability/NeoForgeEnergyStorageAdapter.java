package com.rollylindenshnizzer.nexuscore.neoforge.capability;

import com.rollylindenshnizzer.nexuscore.energy.EnergyStorage;
import net.neoforged.neoforge.energy.IEnergyStorage;

public final class NeoForgeEnergyStorageAdapter implements IEnergyStorage {
    private final EnergyStorage storage;

    public NeoForgeEnergyStorageAdapter(EnergyStorage storage) {
        this.storage = storage;
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        return (int) Math.min(Integer.MAX_VALUE, storage.insert(maxReceive, simulate));
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        return (int) Math.min(Integer.MAX_VALUE, storage.extract(maxExtract, simulate));
    }

    @Override
    public int getEnergyStored() {
        return (int) Math.min(Integer.MAX_VALUE, storage.amount());
    }

    @Override
    public int getMaxEnergyStored() {
        return (int) Math.min(Integer.MAX_VALUE, storage.capacity());
    }

    @Override
    public boolean canExtract() {
        return storage.amount() > 0;
    }

    @Override
    public boolean canReceive() {
        return storage.remainingCapacity() > 0;
    }
}
