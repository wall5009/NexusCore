package com.rollylindenshnizzer.nexuscore.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

public final class RedstoneHelpers {
    public static boolean powered(Level level, BlockPos pos) {
        return level.hasNeighborSignal(pos);
    }

    public static BlockState setPowered(BlockState state, BooleanProperty property, boolean powered) {
        return state.hasProperty(property) ? state.setValue(property, powered) : state;
    }

    public static void notifyNeighbors(Level level, BlockPos pos) {
        level.updateNeighborsAt(pos, level.getBlockState(pos).getBlock());
    }

    public static int comparatorFromProgress(int progress, int maxProgress) {
        if (maxProgress <= 0) {
            return 0;
        }
        return Math.min(15, Math.max(0, (int) Math.ceil(progress * 15.0 / maxProgress)));
    }

    private RedstoneHelpers() {
    }
}
