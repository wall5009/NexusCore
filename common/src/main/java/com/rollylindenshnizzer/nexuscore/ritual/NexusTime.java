package com.rollylindenshnizzer.nexuscore.ritual;

import com.rollylindenshnizzer.nexuscore.api.NexusIncubating;

import java.util.Locale;

@NexusIncubating(since = "1.3")
public enum NexusTime {
    ANY(0, 24_000),
    DAY(0, 12_000),
    NIGHT(12_000, 24_000),
    DAWN(23_000, 1_000),
    DUSK(11_000, 13_000);

    private final int startTick;
    private final int endTick;

    NexusTime(int startTick, int endTick) {
        this.startTick = startTick;
        this.endTick = endTick;
    }

    public int startTick() {
        return startTick;
    }

    public int endTick() {
        return endTick;
    }

    public String serializedName() {
        return name().toLowerCase(Locale.ROOT);
    }
}
