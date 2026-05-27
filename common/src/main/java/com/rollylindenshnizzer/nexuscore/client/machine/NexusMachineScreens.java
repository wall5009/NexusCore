package com.rollylindenshnizzer.nexuscore.client.machine;

import com.rollylindenshnizzer.nexuscore.energy.NexusEnergyStorage;
import com.rollylindenshnizzer.nexuscore.fluid.FluidStack;
import com.rollylindenshnizzer.nexuscore.fluid.NexusFluidTank;
import com.rollylindenshnizzer.nexuscore.inventory.SimpleItemHandler;
import com.rollylindenshnizzer.nexuscore.machine.MachineScreenLayout;
import com.rollylindenshnizzer.nexuscore.machine.MachineState;
import com.rollylindenshnizzer.nexuscore.machine.NexusMachineDefinition;
import com.rollylindenshnizzer.nexuscore.machine.NexusMachines;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluids;

public final class NexusMachineScreens {
    public static void open(NexusMachineDefinition definition,
                            MachineScreenLayout layout,
                            MachineState state,
                            NexusEnergyStorage energy,
                            NexusFluidTank fluid,
                            SimpleItemHandler inventory,
                            Screen parent) {
        Minecraft.getInstance().setScreen(new NexusMachineScreen(definition, layout, state, energy, fluid, inventory, parent));
    }

    public static boolean openFirstPreview(Screen parent) {
        NexusMachineDefinition definition = NexusMachines.definitions().stream().findFirst().orElse(null);
        if (definition == null) {
            return false;
        }
        MachineState state = new MachineState();
        state.start(100);
        state.progress(35, 100);
        NexusEnergyStorage energy = new NexusEnergyStorage(Math.max(1, definition.energyCapacity()));
        energy.insert(Math.max(1, definition.energyCapacity() / 2), false);
        NexusFluidTank fluid = new NexusFluidTank(Math.max(1, definition.fluidCapacity()));
        fluid.fill(new FluidStack(Fluids.WATER, Math.max(0, definition.fluidCapacity() / 2)), false);
        SimpleItemHandler inventory = new SimpleItemHandler(Math.max(1, definition.inventorySize()));
        if (inventory.size() > 0) {
            inventory.set(0, new ItemStack(Items.IRON_INGOT));
        }
        open(definition, MachineScreenLayout.generated(definition), state, energy, fluid, inventory, parent);
        return true;
    }

    private NexusMachineScreens() {
    }
}
