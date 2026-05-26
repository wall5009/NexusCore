package com.rollylindenshnizzer.nexuscore.fabric.transfer;

import com.rollylindenshnizzer.nexuscore.fluid.FluidStack;
import com.rollylindenshnizzer.nexuscore.fluid.FluidTank;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;

import java.util.Iterator;
import java.util.List;

public final class FabricFluidStorageAdapter implements Storage<FluidVariant> {
    private final FluidTank tank;

    public FabricFluidStorageAdapter(FluidTank tank) {
        this.tank = tank;
    }

    @Override
    public long insert(FluidVariant resource, long maxAmount, TransactionContext transaction) {
        FluidStack before = tank.stored();
        long moved = tank.fill(new FluidStack(resource.getFluid(), maxAmount), false);
        transaction.addCloseCallback((ctx, result) -> {
            if (result.wasAborted()) {
                tank.drain(tank.stored().amount(), false);
                tank.fill(before, false);
            }
        });
        return moved;
    }

    @Override
    public long extract(FluidVariant resource, long maxAmount, TransactionContext transaction) {
        if (!tank.stored().isEmpty() && tank.stored().fluid() != resource.getFluid()) {
            return 0;
        }
        FluidStack before = tank.stored();
        long moved = tank.drain(maxAmount, false).amount();
        transaction.addCloseCallback((ctx, result) -> {
            if (result.wasAborted()) {
                tank.drain(tank.stored().amount(), false);
                tank.fill(before, false);
            }
        });
        return moved;
    }

    @Override
    public Iterator<StorageView<FluidVariant>> iterator() {
        return List.<StorageView<FluidVariant>>of(new View()).iterator();
    }

    private final class View implements StorageView<FluidVariant> {
        @Override
        public long extract(FluidVariant resource, long maxAmount, TransactionContext transaction) {
            return FabricFluidStorageAdapter.this.extract(resource, maxAmount, transaction);
        }

        @Override
        public boolean isResourceBlank() {
            return tank.stored().isEmpty();
        }

        @Override
        public FluidVariant getResource() {
            return tank.stored().isEmpty() ? FluidVariant.blank() : FluidVariant.of(tank.stored().fluid());
        }

        @Override
        public long getAmount() {
            return tank.stored().amount();
        }

        @Override
        public long getCapacity() {
            return tank.capacity();
        }
    }
}
