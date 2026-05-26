package com.rollylindenshnizzer.nexuscore.performance;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import java.util.LinkedHashSet;
import java.util.Set;

public final class BatchedBlockUpdates {
    private final Set<BlockPos> positions = new LinkedHashSet<>();

    public void mark(BlockPos pos) {
        positions.add(pos.immutable());
    }

    public int flush(Level level, int flags) {
        int count = 0;
        for (BlockPos pos : positions) {
            level.sendBlockUpdated(pos, level.getBlockState(pos), level.getBlockState(pos), flags);
            count++;
        }
        positions.clear();
        return count;
    }
}
