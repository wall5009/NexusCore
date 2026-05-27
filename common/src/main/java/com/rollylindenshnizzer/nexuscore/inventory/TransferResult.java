package com.rollylindenshnizzer.nexuscore.inventory;

import com.rollylindenshnizzer.nexuscore.api.NexusStable;
import net.minecraft.world.item.ItemStack;

import java.util.List;

@NexusStable(since = "1.2")
public record TransferResult(int moved,
                             ItemStack remaining,
                             boolean completed,
                             List<String> trace) {
    public TransferResult {
        remaining = remaining == null ? ItemStack.EMPTY : remaining.copy();
        trace = trace == null ? List.of() : List.copyOf(trace);
    }
}
