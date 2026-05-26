package com.rollylindenshnizzer.nexuscore.fabric.transfer;

import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;

public final class FabricEnergyStorageAdapter implements team.reborn.energy.api.EnergyStorage {
    private final com.rollylindenshnizzer.nexuscore.energy.EnergyStorage storage;

    public FabricEnergyStorageAdapter(com.rollylindenshnizzer.nexuscore.energy.EnergyStorage storage) {
        this.storage = storage;
    }

    @Override
    public boolean supportsInsertion() {
        return storage.remainingCapacity() > 0;
    }

    @Override
    public long insert(long maxAmount, TransactionContext transaction) {
        long before = storage.amount();
        long moved = storage.insert(maxAmount, false);
        transaction.addCloseCallback((ctx, result) -> {
            if (result.wasAborted()) {
                storage.setAmount(before);
            }
        });
        return moved;
    }

    @Override
    public boolean supportsExtraction() {
        return storage.amount() > 0;
    }

    @Override
    public long extract(long maxAmount, TransactionContext transaction) {
        long before = storage.amount();
        long moved = storage.extract(maxAmount, false);
        transaction.addCloseCallback((ctx, result) -> {
            if (result.wasAborted()) {
                storage.setAmount(before);
            }
        });
        return moved;
    }

    @Override
    public long getAmount() {
        return storage.amount();
    }

    @Override
    public long getCapacity() {
        return storage.capacity();
    }
}
