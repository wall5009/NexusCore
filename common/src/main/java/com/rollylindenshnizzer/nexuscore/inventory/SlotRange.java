package com.rollylindenshnizzer.nexuscore.inventory;

import java.util.stream.IntStream;

public record SlotRange(int startInclusive, int endExclusive) {
    public SlotRange {
        if (startInclusive < 0 || endExclusive < startInclusive) {
            throw new IllegalArgumentException("Invalid slot range " + startInclusive + ".." + endExclusive);
        }
    }

    public boolean contains(int slot) {
        return slot >= startInclusive && slot < endExclusive;
    }

    public int size() {
        return endExclusive - startInclusive;
    }

    public IntStream stream() {
        return IntStream.range(startInclusive, endExclusive);
    }
}
