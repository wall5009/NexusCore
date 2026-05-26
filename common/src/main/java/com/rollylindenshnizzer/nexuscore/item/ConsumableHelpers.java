package com.rollylindenshnizzer.nexuscore.item;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public final class ConsumableHelpers {
    public static ItemStack consumeAndReturn(ItemStack consumed, LivingEntity entity, ItemLike remainder) {
        consumed.shrink(1);
        ItemStack result = new ItemStack(remainder);
        if (consumed.isEmpty()) {
            return result;
        }
        if (entity instanceof net.minecraft.world.entity.player.Player player) {
            if (!player.getInventory().add(result)) {
                player.drop(result, false);
            }
        }
        return consumed;
    }

    private ConsumableHelpers() {
    }
}
