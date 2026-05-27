package com.rollylindenshnizzer.nexuscore.ui.binding;

import com.rollylindenshnizzer.nexuscore.api.NexusStable;
import com.rollylindenshnizzer.nexuscore.energy.NexusEnergyStorage;
import com.rollylindenshnizzer.nexuscore.fluid.NexusFluidTank;
import com.rollylindenshnizzer.nexuscore.machine.MachineState;

import java.util.LinkedHashMap;
import java.util.Map;

@NexusStable(since = "1.2")
public final class MachineUiBindings {
    private final Map<String, ObservableValue<?>> values = new LinkedHashMap<>();

    public static MachineUiBindings machine(MachineState state, NexusEnergyStorage energy, NexusFluidTank fluid) {
        MachineUiBindings bindings = new MachineUiBindings();
        bindings.values.put("machine.progress", new ObservableValue<>(state.progressFraction()));
        bindings.values.put("machine.status", new ObservableValue<>(state.status().name()));
        bindings.values.put("machine.redstone", new ObservableValue<>(state.redstoneMode().name()));
        bindings.values.put("machine.energy", new ObservableValue<>(energy.amount()));
        bindings.values.put("machine.energy.capacity", new ObservableValue<>(energy.capacity()));
        bindings.values.put("machine.fluid.amount", new ObservableValue<>(fluid.stored().amount()));
        bindings.values.put("machine.fluid.capacity", new ObservableValue<>(fluid.capacity()));
        return bindings;
    }

    public Map<String, ObservableValue<?>> values() {
        return Map.copyOf(values);
    }

    public MachineUiBindings bind(String key, ObservableValue<?> value) {
        values.put(key, value);
        return this;
    }
}
