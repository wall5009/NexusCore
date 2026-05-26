package com.rollylindenshnizzer.nexuscore.config;

import java.util.OptionalInt;

public final class IntOption extends ConfigOption<Integer> {
    private Integer min;
    private Integer max;

    IntOption(String key, int defaultValue) {
        super(key, defaultValue);
    }

    public IntOption range(int min, int max) {
        this.min = min;
        this.max = max;
        validator(value -> value >= min && value <= max, "expected " + min + ".." + max);
        return this;
    }

    public OptionalInt min() {
        return min == null ? OptionalInt.empty() : OptionalInt.of(min);
    }

    public OptionalInt max() {
        return max == null ? OptionalInt.empty() : OptionalInt.of(max);
    }

    @Override
    public IntOption serverSynced() {
        super.serverSynced();
        return this;
    }

    @Override
    public IntOption requiresWorldReload() {
        super.requiresWorldReload();
        return this;
    }
}
