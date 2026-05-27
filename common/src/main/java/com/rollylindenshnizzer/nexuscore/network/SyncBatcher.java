package com.rollylindenshnizzer.nexuscore.network;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public final class SyncBatcher<T> {
    private final List<T> queued = new ArrayList<>();
    private final Duration throttle;
    private long lastFlush;

    public SyncBatcher(Duration throttle) {
        this.throttle = throttle;
    }

    public void markDirty(T value) {
        queued.add(value);
    }

    public boolean flushIfReady(Consumer<List<T>> consumer) {
        long now = System.currentTimeMillis();
        if (queued.isEmpty() || now - lastFlush < throttle.toMillis()) {
            return false;
        }
        consumer.accept(List.copyOf(queued));
        queued.clear();
        lastFlush = now;
        return true;
    }
}
