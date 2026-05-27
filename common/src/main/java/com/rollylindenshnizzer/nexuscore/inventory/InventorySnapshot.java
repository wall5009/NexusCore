package com.rollylindenshnizzer.nexuscore.inventory;

import com.rollylindenshnizzer.nexuscore.api.NexusStable;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

@NexusStable(since = "1.2")
public record InventorySnapshot(List<ItemStack> stacks) {
    public InventorySnapshot {
        stacks = stacks.stream().map(ItemStack::copy).toList();
    }

    public static InventorySnapshot capture(SimpleItemHandler inventory) {
        List<ItemStack> stacks = new ArrayList<>();
        for (int slot = 0; slot < inventory.size(); slot++) {
            stacks.add(inventory.get(slot).copy());
        }
        return new InventorySnapshot(stacks);
    }

    public List<Change> diff(InventorySnapshot after) {
        int max = Math.max(stacks.size(), after.stacks.size());
        List<Change> changes = new ArrayList<>();
        for (int slot = 0; slot < max; slot++) {
            ItemStack beforeStack = slot < stacks.size() ? stacks.get(slot) : ItemStack.EMPTY;
            ItemStack afterStack = slot < after.stacks.size() ? after.stacks.get(slot) : ItemStack.EMPTY;
            if (!ItemStack.matches(beforeStack, afterStack)) {
                changes.add(new Change(slot, beforeStack.copy(), afterStack.copy()));
            }
        }
        return changes;
    }

    public record Change(int slot, ItemStack before, ItemStack after) {
    }
}
