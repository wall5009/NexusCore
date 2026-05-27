package com.rollylindenshnizzer.nexuscore.inventory;

import com.rollylindenshnizzer.nexuscore.api.NexusStable;
import net.minecraft.world.item.ItemStack;

import java.util.Objects;
import java.util.function.Predicate;

@NexusStable(since = "1.2")
public record SlotGroup(String name,
                        SlotRange range,
                        SlotRole role,
                        boolean ghost,
                        boolean locked,
                        Predicate<ItemStack> filter) {
    public SlotGroup {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Slot group name cannot be blank");
        }
        Objects.requireNonNull(range, "range");
        role = role == null ? SlotRole.INPUT : role;
        filter = filter == null ? stack -> true : filter;
    }

    public static Builder builder(String name, SlotRange range) {
        return new Builder(name, range);
    }

    public boolean contains(int slot) {
        return range.contains(slot);
    }

    public boolean accepts(ItemStack stack) {
        return !locked && filter.test(stack);
    }

    public static final class Builder {
        private final String name;
        private final SlotRange range;
        private SlotRole role = SlotRole.INPUT;
        private boolean ghost;
        private boolean locked;
        private Predicate<ItemStack> filter = stack -> true;

        private Builder(String name, SlotRange range) {
            this.name = name;
            this.range = range;
        }

        public Builder role(SlotRole role) {
            this.role = role;
            return this;
        }

        public Builder ghost() {
            this.ghost = true;
            this.role = SlotRole.GHOST;
            return this;
        }

        public Builder locked() {
            this.locked = true;
            return this;
        }

        public Builder filter(Predicate<ItemStack> filter) {
            this.filter = filter;
            return this;
        }

        public SlotGroup build() {
            return new SlotGroup(name, range, role, ghost, locked, filter);
        }
    }
}
