package com.rollylindenshnizzer.nexuscore.performance;

import java.time.Duration;
import java.util.function.Supplier;

public final class ExpiringCache<T> {
    private final Duration ttl;
    private final Supplier<T> supplier;
    private long expiresAt;
    private T value;

    public ExpiringCache(Duration ttl, Supplier<T> supplier) {
        this.ttl = ttl;
        this.supplier = supplier;
    }

    public T get() {
        long now = System.currentTimeMillis();
        if (value == null || now >= expiresAt) {
            value = supplier.get();
            expiresAt = now + ttl.toMillis();
        }
        return value;
    }
}
