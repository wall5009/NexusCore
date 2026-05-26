package com.rollylindenshnizzer.nexuscore.machine;

import com.rollylindenshnizzer.nexuscore.energy.EnergyStorage;
import com.rollylindenshnizzer.nexuscore.fluid.FluidTank;
import com.rollylindenshnizzer.nexuscore.inventory.SimpleItemHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public abstract class BaseMachineBlockEntity extends BlockEntity {
    protected final MachineState machineState = new MachineState();
    protected final EnergyStorage energy;
    protected final FluidTank fluidTank;
    protected final SimpleItemHandler inventory;

    protected BaseMachineBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state,
                                     long energyCapacity, long fluidCapacity, int inventorySize) {
        super(type, pos, state);
        this.energy = new EnergyStorage(energyCapacity);
        this.fluidTank = new FluidTank(fluidCapacity);
        this.inventory = new SimpleItemHandler(inventorySize);
    }

    public MachineState machineState() {
        return machineState;
    }

    public EnergyStorage energy() {
        return energy;
    }

    public FluidTank fluidTank() {
        return fluidTank;
    }

    public SimpleItemHandler inventory() {
        return inventory;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("Machine", machineState.save());
        tag.put("Energy", energy.save());
        tag.put("FluidTank", fluidTank.save());
        tag.put("Inventory", inventory.save(registries));
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        machineState.load(tag.getCompound("Machine"));
        energy.load(tag.getCompound("Energy"));
        fluidTank.load(tag.getCompound("FluidTank"));
        inventory.load(tag.getCompound("Inventory"), registries);
    }
}
