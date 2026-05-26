package com.rollylindenshnizzer.nexuscore.block;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.context.UseOnContext;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public final class BlockInteractionBuilder {
    private final List<Rule> rules = new ArrayList<>();

    public BlockInteractionBuilder when(Predicate<UseOnContext> predicate, ResultFactory result) {
        rules.add(new Rule(predicate, result));
        return this;
    }

    public InteractionResult use(UseOnContext context) {
        for (Rule rule : rules) {
            if (rule.predicate().test(context)) {
                return rule.result().apply(context);
            }
        }
        return InteractionResult.PASS;
    }

    public static Predicate<UseOnContext> sneaking() {
        return context -> context.getPlayer() != null && context.getPlayer().isShiftKeyDown();
    }

    public static Predicate<UseOnContext> serverSide() {
        return context -> !context.getLevel().isClientSide();
    }

    @FunctionalInterface
    public interface ResultFactory {
        InteractionResult apply(UseOnContext context);
    }

    private record Rule(Predicate<UseOnContext> predicate, ResultFactory result) {
    }
}
