package com.rollylindenshnizzer.nexuscore.inventory;

import com.rollylindenshnizzer.nexuscore.api.NexusStable;
import net.minecraft.world.item.ItemStack;

import java.util.Objects;
import java.util.function.Predicate;

@NexusStable(since = "1.2")
public record TransferRule(String name,
                           SlotRange from,
                           SlotRange to,
                           Predicate<ItemStack> predicate,
                           boolean automation,
                           boolean player,
                           boolean hopper,
                           int maxPerOperation) {
    public TransferRule {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Transfer rule name cannot be blank");
        }
        Objects.requireNonNull(from, "from");
        Objects.requireNonNull(to, "to");
        predicate = predicate == null ? stack -> true : predicate;
        maxPerOperation = Math.max(1, maxPerOperation);
    }

    public static Builder builder(String name, SlotRange from, SlotRange to) {
        return new Builder(name, from, to);
    }

    public boolean accepts(int sourceSlot, ItemStack stack, TransferActor actor) {
        if (!from.contains(sourceSlot) || !predicate.test(stack)) {
            return false;
        }
        return switch (actor) {
            case AUTOMATION -> automation;
            case HOPPER -> hopper;
            case PLAYER -> player;
        };
    }

    public enum TransferActor {
        AUTOMATION,
        HOPPER,
        PLAYER
    }

    public static final class Builder {
        private final String name;
        private final SlotRange from;
        private final SlotRange to;
        private Predicate<ItemStack> predicate = stack -> true;
        private boolean automation = true;
        private boolean player = true;
        private boolean hopper = true;
        private int maxPerOperation = 64;

        private Builder(String name, SlotRange from, SlotRange to) {
            this.name = name;
            this.from = from;
            this.to = to;
        }

        public Builder predicate(Predicate<ItemStack> predicate) {
            this.predicate = predicate;
            return this;
        }

        public Builder automation(boolean automation) {
            this.automation = automation;
            return this;
        }

        public Builder player(boolean player) {
            this.player = player;
            return this;
        }

        public Builder hopper(boolean hopper) {
            this.hopper = hopper;
            return this;
        }

        public Builder maxPerOperation(int maxPerOperation) {
            this.maxPerOperation = maxPerOperation;
            return this;
        }

        public TransferRule build() {
            return new TransferRule(name, from, to, predicate, automation, player, hopper, maxPerOperation);
        }
    }
}
