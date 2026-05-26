package com.rollylindenshnizzer.nexuscore.neoforge.capability;

import com.rollylindenshnizzer.nexuscore.fluid.FluidTank;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

public final class NeoForgeFluidHandlerAdapter implements IFluidHandler {
    private final FluidTank tank;

    public NeoForgeFluidHandlerAdapter(FluidTank tank) {
        this.tank = tank;
    }

    @Override
    public int getTanks() {
        return 1;
    }

    @Override
    public FluidStack getFluidInTank(int tank) {
        if (tank != 0 || this.tank.stored().isEmpty()) {
            return FluidStack.EMPTY;
        }
        return new FluidStack(this.tank.stored().fluid(), clamp(this.tank.stored().amount()));
    }

    @Override
    public int getTankCapacity(int tank) {
        return tank == 0 ? clamp(this.tank.capacity()) : 0;
    }

    @Override
    public boolean isFluidValid(int tank, FluidStack stack) {
        return tank == 0 && this.tank.accepts(stack.getFluid());
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        return clamp(tank.fill(new com.rollylindenshnizzer.nexuscore.fluid.FluidStack(resource.getFluid(), resource.getAmount()), action.simulate()));
    }

    @Override
    public FluidStack drain(FluidStack resource, FluidAction action) {
        if (!tank.stored().isEmpty() && tank.stored().fluid() != resource.getFluid()) {
            return FluidStack.EMPTY;
        }
        com.rollylindenshnizzer.nexuscore.fluid.FluidStack drained = tank.drain(resource.getAmount(), action.simulate());
        return drained.isEmpty() ? FluidStack.EMPTY : new FluidStack(drained.fluid(), clamp(drained.amount()));
    }

    @Override
    public FluidStack drain(int maxDrain, FluidAction action) {
        com.rollylindenshnizzer.nexuscore.fluid.FluidStack drained = tank.drain(maxDrain, action.simulate());
        return drained.isEmpty() ? FluidStack.EMPTY : new FluidStack(drained.fluid(), clamp(drained.amount()));
    }

    private static int clamp(long amount) {
        return (int) Math.max(0, Math.min(Integer.MAX_VALUE, amount));
    }
}
