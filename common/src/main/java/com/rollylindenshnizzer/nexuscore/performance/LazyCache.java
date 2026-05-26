package com.rollylindenshnizzer.nexuscore.performance;

import java.util.function.Supplier;

public final class LazyCache<T> {
    private final Supplier<T> supplier;
    private T value;
    private boolean initialized;

    public LazyCache(Supplier<T> supplier) {
        this.supplier = supplier;
    }

    public T get() {
        if (!initialized) {
            value = supplier.get();
            initialized = true;
        }
        return value;
    }

    public void invalidate() {
        initialized = false;
        value = null;
    }
}
