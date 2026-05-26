package com.rollylindenshnizzer.nexuscore.debug;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public final class DebugInspector {
    public static String item(ItemStack stack) {
        return stack.getCount() + "x " + stack.getItem() + " components=" + stack.getComponentsPatch();
    }

    public static String block(Level level, BlockPos pos) {
        return level.getBlockState(pos).toString();
    }

    public static String entity(Entity entity) {
        return entity.getType() + " uuid=" + entity.getUUID() + " pos=" + entity.position();
    }

    private DebugInspector() {
    }
}
