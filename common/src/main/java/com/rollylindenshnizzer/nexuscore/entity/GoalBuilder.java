package com.rollylindenshnizzer.nexuscore.entity;

import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;
import java.util.function.BooleanSupplier;

public final class GoalBuilder {
    private BooleanSupplier canUse = () -> true;
    private BooleanSupplier canContinue = () -> false;
    private Runnable start = () -> {};
    private Runnable stop = () -> {};
    private Runnable tick = () -> {};
    private EnumSet<Goal.Flag> flags = EnumSet.noneOf(Goal.Flag.class);

    public GoalBuilder canUse(BooleanSupplier canUse) {
        this.canUse = canUse;
        return this;
    }

    public GoalBuilder canContinue(BooleanSupplier canContinue) {
        this.canContinue = canContinue;
        return this;
    }

    public GoalBuilder start(Runnable start) {
        this.start = start;
        return this;
    }

    public GoalBuilder stop(Runnable stop) {
        this.stop = stop;
        return this;
    }

    public GoalBuilder tick(Runnable tick) {
        this.tick = tick;
        return this;
    }

    public GoalBuilder flags(Goal.Flag... flags) {
        this.flags = flags.length == 0 ? EnumSet.noneOf(Goal.Flag.class) : EnumSet.of(flags[0], flags);
        return this;
    }

    public Goal build() {
        Goal goal = new Goal() {
            @Override
            public boolean canUse() {
                return canUse.getAsBoolean();
            }

            @Override
            public boolean canContinueToUse() {
                return canContinue.getAsBoolean();
            }

            @Override
            public void start() {
                start.run();
            }

            @Override
            public void stop() {
                stop.run();
            }

            @Override
            public void tick() {
                tick.run();
            }
        };
        goal.setFlags(flags);
        return goal;
    }
}
