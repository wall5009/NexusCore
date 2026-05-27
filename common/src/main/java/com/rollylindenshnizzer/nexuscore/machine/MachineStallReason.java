package com.rollylindenshnizzer.nexuscore.machine;

import com.rollylindenshnizzer.nexuscore.api.NexusStable;

@NexusStable(since = "1.2")
public enum MachineStallReason {
    NONE,
    REDSTONE_BLOCKED,
    NO_RECIPE,
    MISSING_ITEM_INPUT,
    MISSING_FLUID_INPUT,
    NOT_ENOUGH_ENERGY,
    OUTPUT_BLOCKED,
    INVALID_SIDE,
    PAUSED,
    ERROR
}
