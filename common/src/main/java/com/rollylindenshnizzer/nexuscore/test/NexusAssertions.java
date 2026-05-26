package com.rollylindenshnizzer.nexuscore.test;

import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

public final class NexusAssertions {
    public static void block(Level level, BlockPos pos, Block expected) {
        if (!level.getBlockState(pos).is(expected)) {
            throw new AssertionError("Expected block " + expected + " at " + pos + " but found " + level.getBlockState(pos));
        }
    }

    public static void contains(Container container, Item item) {
        for (int slot = 0; slot < container.getContainerSize(); slot++) {
            ItemStack stack = container.getItem(slot);
            if (stack.is(item)) {
                return;
            }
        }
        throw new AssertionError("Expected inventory to contain " + item);
    }

    private NexusAssertions() {
    }
}
