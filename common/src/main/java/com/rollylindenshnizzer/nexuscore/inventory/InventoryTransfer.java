package com.rollylindenshnizzer.nexuscore.inventory;

import com.rollylindenshnizzer.nexuscore.api.NexusStable;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

@NexusStable(since = "1.2")
public final class InventoryTransfer {
    public static TransferResult route(SimpleItemHandler inventory,
                                       int sourceSlot,
                                       TransferRule.TransferActor actor,
                                       List<TransferRule> rules,
                                       boolean simulate) {
        ItemStack source = inventory.get(sourceSlot);
        if (source.isEmpty()) {
            return new TransferResult(0, ItemStack.EMPTY, true, List.of("source slot is empty"));
        }

        List<String> trace = new ArrayList<>();
        ItemStack remaining = source.copyWithCount(Math.min(source.getCount(), maxAllowed(source, source.getCount())));
        int originalCount = remaining.getCount();
        for (TransferRule rule : rules) {
            if (!rule.accepts(sourceSlot, source, actor)) {
                trace.add(rule.name() + ": skipped");
                continue;
            }
            ItemStack limited = remaining.copyWithCount(Math.min(remaining.getCount(), rule.maxPerOperation()));
            ItemStack after = insertInto(inventory, rule.to(), limited, simulate, trace, rule.name());
            int movedForRule = limited.getCount() - after.getCount();
            remaining.shrink(movedForRule);
            if (remaining.isEmpty()) {
                break;
            }
        }

        int moved = originalCount - remaining.getCount();
        if (!simulate && moved > 0) {
            inventory.extract(sourceSlot, moved, false);
        }
        return new TransferResult(moved, source.copyWithCount(source.getCount() - moved), moved > 0, trace);
    }

    public static TransferResult move(SimpleItemHandler from,
                                      int sourceSlot,
                                      SimpleItemHandler to,
                                      SlotRange target,
                                      int maxAmount,
                                      boolean simulate) {
        ItemStack source = from.get(sourceSlot);
        if (source.isEmpty() || maxAmount <= 0) {
            return new TransferResult(0, ItemStack.EMPTY, true, List.of("nothing to move"));
        }
        List<String> trace = new ArrayList<>();
        ItemStack movedStack = source.copyWithCount(Math.min(source.getCount(), maxAmount));
        ItemStack remaining = insertInto(to, target, movedStack, simulate, trace, "direct");
        int moved = movedStack.getCount() - remaining.getCount();
        if (!simulate && moved > 0) {
            from.extract(sourceSlot, moved, false);
        }
        return new TransferResult(moved, source.copyWithCount(source.getCount() - moved), moved > 0, trace);
    }

    public static ItemStack insertInto(SimpleItemHandler inventory,
                                       SlotRange target,
                                       ItemStack stack,
                                       boolean simulate,
                                       List<String> trace,
                                       String ruleName) {
        ItemStack remaining = stack.copy();
        for (int slot = target.startInclusive(); slot < target.endExclusive() && !remaining.isEmpty(); slot++) {
            if (slot < 0 || slot >= inventory.size()) {
                trace.add(ruleName + ": target slot " + slot + " out of bounds");
                continue;
            }
            int before = remaining.getCount();
            remaining = inventory.insert(slot, remaining, simulate);
            int moved = before - remaining.getCount();
            if (moved > 0) {
                trace.add(ruleName + ": moved " + moved + " into slot " + slot);
            }
        }
        return remaining;
    }

    private static int maxAllowed(ItemStack stack, int requested) {
        return Math.min(requested, stack.getMaxStackSize());
    }

    private InventoryTransfer() {
    }
}
