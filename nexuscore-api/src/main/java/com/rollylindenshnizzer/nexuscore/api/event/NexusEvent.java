package com.rollylindenshnizzer.nexuscore.api.event;

public interface NexusEvent<T> {
    void register(T listener);
}
