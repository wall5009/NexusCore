package com.rollylindenshnizzer.nexuscore.fluid;

import com.rollylindenshnizzer.nexuscore.api.NexusStable;

@NexusStable(since = "1.2")
public enum FluidAccess {
    NONE,
    INPUT,
    OUTPUT,
    BOTH;

    public boolean canFill() {
        return this == INPUT || this == BOTH;
    }

    public boolean canDrain() {
        return this == OUTPUT || this == BOTH;
    }
}
