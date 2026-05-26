package com.rollylindenshnizzer.nexuscore.item;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public final class ItemInteractionBuilder {
    private final List<Rule> rules = new ArrayList<>();

    public ItemInteractionBuilder when(Predicate<Context> predicate, Action action) {
        rules.add(new Rule(predicate, action));
        return this;
    }

    public ItemInteractionBuilder serverOnly(Action action) {
        return when(context -> !context.level().isClientSide(), action);
    }

    public NexusItem.UseHandler build() {
        return (level, player, hand, stack) -> {
            Context context = new Context(level, player, hand, stack);
            for (Rule rule : rules) {
                if (rule.predicate().test(context)) {
                    return rule.action().apply(context);
                }
            }
            return InteractionResultHolder.pass(stack);
        };
    }

    public static Action sound(SoundEvent sound, float volume, float pitch) {
        return context -> {
            context.level().playSound(null, context.player().blockPosition(), sound, SoundSource.PLAYERS, volume, pitch);
            return InteractionResultHolder.success(context.stack());
        };
    }

    public record Context(Level level, Player player, InteractionHand hand, ItemStack stack) {
    }

    @FunctionalInterface
    public interface Action {
        InteractionResultHolder<ItemStack> apply(Context context);
    }

    private record Rule(Predicate<Context> predicate, Action action) {
    }
}
