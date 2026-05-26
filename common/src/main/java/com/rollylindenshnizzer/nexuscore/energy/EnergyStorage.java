package com.rollylindenshnizzer.nexuscore.energy;

import net.minecraft.nbt.CompoundTag;

public class EnergyStorage {
    private final long capacity;
    private final long maxInsert;
    private final long maxExtract;
    private long amount;

    public EnergyStorage(long capacity) {
        this(capacity, capacity, capacity);
    }

    public EnergyStorage(long capacity, long maxInsert, long maxExtract) {
        if (capacity < 0 || maxInsert < 0 || maxExtract < 0) {
            throw new IllegalArgumentException("Energy values must be non-negative");
        }
        this.capacity = capacity;
        this.maxInsert = maxInsert;
        this.maxExtract = maxExtract;
    }

    public long amount() {
        return amount;
    }

    public long capacity() {
        return capacity;
    }

    public long remainingCapacity() {
        return capacity - amount;
    }

    public long insert(long requested, boolean simulate) {
        long moved = Math.min(Math.min(requested, maxInsert), remainingCapacity());
        if (!simulate) {
            amount += moved;
        }
        return moved;
    }

    public long extract(long requested, boolean simulate) {
        long moved = Math.min(Math.min(requested, maxExtract), amount);
        if (!simulate) {
            amount -= moved;
        }
        return moved;
    }

    public void setAmount(long amount) {
        this.amount = Math.max(0, Math.min(capacity, amount));
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putLong("Amount", amount);
        tag.putLong("Capacity", capacity);
        return tag;
    }

    public void load(CompoundTag tag) {
        setAmount(tag.getLong("Amount"));
    }
}
