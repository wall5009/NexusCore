package com.rollylindenshnizzer.nexuscore.performance;

import java.util.function.Supplier;

public final class ReloadAwareMemoizedSupplier<T> implements Supplier<T> {
    private final Supplier<T> factory;
    private T value;
    private boolean initialized;

    public ReloadAwareMemoizedSupplier(Supplier<T> factory) {
        this.factory = factory;
    }

    @Override
    public T get() {
        if (!initialized) {
            value = factory.get();
            initialized = true;
        }
        return value;
    }

    public void invalidate() {
        value = null;
        initialized = false;
    }
}
