package com.rollylindenshnizzer.nexuscore.fluid;

import com.rollylindenshnizzer.nexuscore.api.NexusStable;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.material.Fluid;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;

@NexusStable(since = "1.2")
public class NexusFluidTank extends FluidTank {
    private final EnumMap<Direction, FluidAccess> sideAccess = new EnumMap<>(Direction.class);
    private final List<Consumer<FluidStack>> listeners = new ArrayList<>();
    private Predicate<Fluid> filter = fluid -> true;
    private boolean locked;

    public NexusFluidTank(long capacity) {
        super(capacity);
        for (Direction direction : Direction.values()) {
            sideAccess.put(direction, FluidAccess.BOTH);
        }
    }

    public static Builder builder(long capacity) {
        return new Builder(capacity);
    }

    @Override
    public boolean accepts(Fluid fluid) {
        return !locked && filter.test(fluid) && super.accepts(fluid);
    }

    @Override
    public long fill(FluidStack stack, boolean simulate) {
        FluidStack before = stored();
        long moved = super.fill(stack, simulate);
        fireIfChanged(before, simulate);
        return moved;
    }

    @Override
    public FluidStack drain(long requested, boolean simulate) {
        FluidStack before = stored();
        FluidStack drained = super.drain(requested, simulate);
        fireIfChanged(before, simulate);
        return drained;
    }

    public long fill(Direction side, FluidStack stack, boolean simulate) {
        if (side != null && !access(side).canFill()) {
            return 0;
        }
        return fill(stack, simulate);
    }

    public FluidStack drain(Direction side, long requested, boolean simulate) {
        if (side != null && !access(side).canDrain()) {
            return FluidStack.EMPTY;
        }
        return drain(requested, simulate);
    }

    public NexusFluidTank filter(Predicate<Fluid> filter) {
        this.filter = filter == null ? fluid -> true : filter;
        return this;
    }

    public NexusFluidTank locked(boolean locked) {
        this.locked = locked;
        return this;
    }

    public NexusFluidTank side(Direction direction, FluidAccess access) {
        sideAccess.put(Objects.requireNonNull(direction, "direction"), access == null ? FluidAccess.NONE : access);
        return this;
    }

    public FluidAccess access(Direction direction) {
        return sideAccess.getOrDefault(direction, FluidAccess.NONE);
    }

    public NexusFluidTank onChanged(Consumer<FluidStack> listener) {
        listeners.add(Objects.requireNonNull(listener, "listener"));
        return this;
    }

    public List<Component> tooltip() {
        FluidStack stored = stored();
        String fluidName = stored.isEmpty() ? "Empty" : stored.fluid().defaultFluidState().toString();
        return List.of(Component.literal(fluidName + " " + FluidFormat.millibuckets(stored.amount()) + " / " + FluidFormat.millibuckets(capacity())));
    }

    private void fireIfChanged(FluidStack before, boolean simulate) {
        if (!simulate && !before.equals(stored())) {
            listeners.forEach(listener -> listener.accept(stored()));
        }
    }

    public static final class Builder {
        private final long capacity;
        private Predicate<Fluid> filter = fluid -> true;
        private boolean locked;
        private final EnumMap<Direction, FluidAccess> sides = new EnumMap<>(Direction.class);

        private Builder(long capacity) {
            this.capacity = capacity;
        }

        public Builder filter(Predicate<Fluid> filter) {
            this.filter = filter;
            return this;
        }

        public Builder locked(boolean locked) {
            this.locked = locked;
            return this;
        }

        public Builder side(Direction direction, FluidAccess access) {
            sides.put(direction, access);
            return this;
        }

        public NexusFluidTank build() {
            NexusFluidTank tank = new NexusFluidTank(capacity).filter(filter).locked(locked);
            sides.forEach(tank::side);
            return tank;
        }
    }
}
