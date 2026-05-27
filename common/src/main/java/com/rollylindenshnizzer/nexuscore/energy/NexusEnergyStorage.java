package com.rollylindenshnizzer.nexuscore.energy;

import com.rollylindenshnizzer.nexuscore.api.NexusStable;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Objects;
import java.util.function.LongConsumer;

@NexusStable(since = "1.2")
public class NexusEnergyStorage extends EnergyStorage {
    private final EnumMap<Direction, EnergyAccess> sideAccess = new EnumMap<>(Direction.class);
    private final List<LongConsumer> listeners = new ArrayList<>();
    private long networkRateLimit = Long.MAX_VALUE;

    public NexusEnergyStorage(long capacity) {
        this(capacity, capacity, capacity);
    }

    public NexusEnergyStorage(long capacity, long maxInsert, long maxExtract) {
        super(capacity, maxInsert, maxExtract);
        for (Direction direction : Direction.values()) {
            sideAccess.put(direction, EnergyAccess.BOTH);
        }
    }

    public static Builder builder(long capacity) {
        return new Builder(capacity);
    }

    @Override
    public long insert(long requested, boolean simulate) {
        long before = amount();
        long moved = super.insert(Math.min(requested, networkRateLimit), simulate);
        fireIfChanged(before, simulate);
        return moved;
    }

    @Override
    public long extract(long requested, boolean simulate) {
        long before = amount();
        long moved = super.extract(Math.min(requested, networkRateLimit), simulate);
        fireIfChanged(before, simulate);
        return moved;
    }

    public long insert(Direction side, long requested, boolean simulate) {
        if (side != null && !access(side).canInsert()) {
            return 0;
        }
        return insert(requested, simulate);
    }

    public long extract(Direction side, long requested, boolean simulate) {
        if (side != null && !access(side).canExtract()) {
            return 0;
        }
        return extract(requested, simulate);
    }

    public NexusEnergyStorage side(Direction direction, EnergyAccess access) {
        sideAccess.put(Objects.requireNonNull(direction, "direction"), access == null ? EnergyAccess.NONE : access);
        return this;
    }

    public EnergyAccess access(Direction direction) {
        return sideAccess.getOrDefault(direction, EnergyAccess.NONE);
    }

    public NexusEnergyStorage rateLimit(long maxPerOperation) {
        networkRateLimit = Math.max(0, maxPerOperation);
        return this;
    }

    public NexusEnergyStorage onChanged(LongConsumer listener) {
        listeners.add(Objects.requireNonNull(listener, "listener"));
        return this;
    }

    public List<Component> tooltip() {
        return List.of(Component.literal(EnergyFormat.format(amount()) + " / " + EnergyFormat.format(capacity())));
    }

    private void fireIfChanged(long before, boolean simulate) {
        if (!simulate && before != amount()) {
            listeners.forEach(listener -> listener.accept(amount()));
        }
    }

    public static final class Builder {
        private final long capacity;
        private long maxInsert;
        private long maxExtract;
        private long rateLimit = Long.MAX_VALUE;
        private final EnumMap<Direction, EnergyAccess> sides = new EnumMap<>(Direction.class);

        private Builder(long capacity) {
            this.capacity = capacity;
            this.maxInsert = capacity;
            this.maxExtract = capacity;
        }

        public Builder io(long maxInsert, long maxExtract) {
            this.maxInsert = maxInsert;
            this.maxExtract = maxExtract;
            return this;
        }

        public Builder side(Direction direction, EnergyAccess access) {
            sides.put(direction, access);
            return this;
        }

        public Builder rateLimit(long rateLimit) {
            this.rateLimit = rateLimit;
            return this;
        }

        public NexusEnergyStorage build() {
            NexusEnergyStorage storage = new NexusEnergyStorage(capacity, maxInsert, maxExtract).rateLimit(rateLimit);
            sides.forEach(storage::side);
            return storage;
        }
    }
}
