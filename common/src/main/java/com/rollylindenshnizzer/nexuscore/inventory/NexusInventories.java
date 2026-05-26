package com.rollylindenshnizzer.nexuscore.inventory;

import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

import java.util.OptionalInt;
import java.util.function.Predicate;

public final class NexusInventories {
    public static OptionalInt firstMatching(Container container, Predicate<ItemStack> predicate) {
        for (int slot = 0; slot < container.getContainerSize(); slot++) {
            if (predicate.test(container.getItem(slot))) {
                return OptionalInt.of(slot);
            }
        }
        return OptionalInt.empty();
    }

    public static boolean canMerge(ItemStack left, ItemStack right) {
        return ItemStack.isSameItemSameComponents(left, right) && left.getCount() + right.getCount() <= left.getMaxStackSize();
    }

    public static int insertSimulated(Container container, ItemStack stack) {
        int remaining = stack.getCount();
        for (int slot = 0; slot < container.getContainerSize() && remaining > 0; slot++) {
            ItemStack existing = container.getItem(slot);
            if (existing.isEmpty()) {
                remaining = 0;
            } else if (ItemStack.isSameItemSameComponents(existing, stack)) {
                remaining = Math.max(0, remaining - (existing.getMaxStackSize() - existing.getCount()));
            }
        }
        return remaining;
    }

    private NexusInventories() {
    }
}
