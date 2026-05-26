package com.rollylindenshnizzer.nexuscore.performance;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

public final class NexusRateLimiter {
    private final Duration interval;
    private final Map<String, Long> lastAllowed = new HashMap<>();

    public NexusRateLimiter(Duration interval) {
        this.interval = interval;
    }

    public boolean tryAcquire(String key) {
        long now = System.currentTimeMillis();
        long last = lastAllowed.getOrDefault(key, 0L);
        if (now - last >= interval.toMillis()) {
            lastAllowed.put(key, now);
            return true;
        }
        return false;
    }
}
