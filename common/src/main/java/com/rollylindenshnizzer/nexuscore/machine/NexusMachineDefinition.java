package com.rollylindenshnizzer.nexuscore.machine;

import com.rollylindenshnizzer.nexuscore.api.NexusStable;
import com.rollylindenshnizzer.nexuscore.inventory.SlotGroup;
import com.rollylindenshnizzer.nexuscore.inventory.SlotRange;
import com.rollylindenshnizzer.nexuscore.inventory.SlotRole;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@NexusStable(since = "1.2")
public record NexusMachineDefinition(ResourceLocation id,
                                     String category,
                                     long energyCapacity,
                                     long maxEnergyInput,
                                     long maxEnergyOutput,
                                     long fluidCapacity,
                                     int inventorySize,
                                     List<SlotGroup> slotGroups,
                                     Map<Direction, SideConfiguration> sideConfiguration,
                                     List<MachineUpgrade> upgrades,
                                     boolean generatedScreen,
                                     boolean comparatorOutput,
                                     RedstoneControlMode defaultRedstoneMode) {
    public NexusMachineDefinition {
        category = category == null || category.isBlank() ? "machine" : category;
        energyCapacity = Math.max(0, energyCapacity);
        maxEnergyInput = Math.max(0, maxEnergyInput);
        maxEnergyOutput = Math.max(0, maxEnergyOutput);
        fluidCapacity = Math.max(0, fluidCapacity);
        inventorySize = Math.max(0, inventorySize);
        slotGroups = slotGroups == null ? List.of() : List.copyOf(slotGroups);
        sideConfiguration = sideConfiguration == null ? Map.of() : Map.copyOf(sideConfiguration);
        upgrades = upgrades == null ? List.of() : List.copyOf(upgrades);
        defaultRedstoneMode = defaultRedstoneMode == null ? RedstoneControlMode.IGNORED : defaultRedstoneMode;
    }

    public static Builder builder(ResourceLocation id) {
        return new Builder(id);
    }

    public List<Integer> slotsWithRole(SlotRole role) {
        List<Integer> slots = new ArrayList<>();
        for (SlotGroup group : slotGroups) {
            if (group.role() == role) {
                group.range().stream().forEach(slots::add);
            }
        }
        return slots;
    }

    public SideConfiguration side(Direction direction) {
        return sideConfiguration.getOrDefault(direction, SideConfiguration.BOTH);
    }

    public static final class Builder {
        private final ResourceLocation id;
        private String category = "machine";
        private long energyCapacity;
        private long maxEnergyInput;
        private long maxEnergyOutput;
        private long fluidCapacity;
        private int inventorySize;
        private final List<SlotGroup> slotGroups = new ArrayList<>();
        private final EnumMap<Direction, SideConfiguration> sides = new EnumMap<>(Direction.class);
        private final List<MachineUpgrade> upgrades = new ArrayList<>();
        private boolean generatedScreen = true;
        private boolean comparatorOutput = true;
        private RedstoneControlMode defaultRedstoneMode = RedstoneControlMode.IGNORED;

        private Builder(ResourceLocation id) {
            this.id = id;
            for (Direction direction : Direction.values()) {
                sides.put(direction, SideConfiguration.BOTH);
            }
        }

        public Builder category(String category) {
            this.category = category;
            return this;
        }

        public Builder energy(long capacity, long maxInput, long maxOutput) {
            this.energyCapacity = capacity;
            this.maxEnergyInput = maxInput;
            this.maxEnergyOutput = maxOutput;
            return this;
        }

        public Builder fluid(long capacity) {
            this.fluidCapacity = capacity;
            return this;
        }

        public Builder inventory(int size) {
            this.inventorySize = size;
            return this;
        }

        public Builder slots(String name, SlotRole role, int startInclusive, int endExclusive) {
            slotGroups.add(SlotGroup.builder(name, new SlotRange(startInclusive, endExclusive)).role(role).build());
            inventorySize = Math.max(inventorySize, endExclusive);
            return this;
        }

        public Builder slotGroup(SlotGroup group) {
            slotGroups.add(group);
            inventorySize = Math.max(inventorySize, group.range().endExclusive());
            return this;
        }

        public Builder side(Direction direction, SideConfiguration configuration) {
            sides.put(direction, configuration);
            return this;
        }

        public Builder upgrade(MachineUpgrade upgrade) {
            upgrades.add(upgrade);
            return this;
        }

        public Builder generatedScreen(boolean generatedScreen) {
            this.generatedScreen = generatedScreen;
            return this;
        }

        public Builder comparatorOutput(boolean comparatorOutput) {
            this.comparatorOutput = comparatorOutput;
            return this;
        }

        public Builder redstone(RedstoneControlMode mode) {
            this.defaultRedstoneMode = mode;
            return this;
        }

        public NexusMachineDefinition build() {
            return new NexusMachineDefinition(id, category, energyCapacity, maxEnergyInput, maxEnergyOutput,
                    fluidCapacity, inventorySize, slotGroups, sides, upgrades, generatedScreen, comparatorOutput,
                    defaultRedstoneMode);
        }
    }
}
