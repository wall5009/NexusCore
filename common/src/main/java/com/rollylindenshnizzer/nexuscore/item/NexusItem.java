package com.rollylindenshnizzer.nexuscore.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

public class NexusItem extends Item {
    private final List<Component> tooltip;
    private final UseHandler useHandler;

    public NexusItem(Properties properties, List<Component> tooltip, UseHandler useHandler) {
        super(properties);
        this.tooltip = tooltip;
        this.useHandler = useHandler;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> components, TooltipFlag flag) {
        components.addAll(tooltip);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
        if (useHandler != null) {
            return useHandler.use(level, player, usedHand, player.getItemInHand(usedHand));
        }
        return super.use(level, player, usedHand);
    }

    @FunctionalInterface
    public interface UseHandler {
        InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand, ItemStack stack);
    }
}
