package com.rollylindenshnizzer.nexuscore.energy;

import com.rollylindenshnizzer.nexuscore.api.NexusStable;

@NexusStable(since = "1.2")
public enum EnergyAccess {
    NONE,
    INPUT,
    OUTPUT,
    BOTH;

    public boolean canInsert() {
        return this == INPUT || this == BOTH;
    }

    public boolean canExtract() {
        return this == OUTPUT || this == BOTH;
    }
}
