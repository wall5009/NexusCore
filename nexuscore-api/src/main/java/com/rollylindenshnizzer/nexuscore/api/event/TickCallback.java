package com.rollylindenshnizzer.nexuscore.api.event;

@FunctionalInterface
public interface TickCallback {
    void onTick(long tick);
}
