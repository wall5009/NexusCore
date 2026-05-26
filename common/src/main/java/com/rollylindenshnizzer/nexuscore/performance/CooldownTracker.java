package com.rollylindenshnizzer.nexuscore.performance;

import java.util.HashMap;
import java.util.Map;

public final class CooldownTracker<K> {
    private final Map<K, Long> cooldowns = new HashMap<>();

    public boolean ready(K key, long currentTick) {
        return cooldowns.getOrDefault(key, 0L) <= currentTick;
    }

    public void set(K key, long currentTick, long durationTicks) {
        cooldowns.put(key, currentTick + durationTicks);
    }

    public long remaining(K key, long currentTick) {
        return Math.max(0, cooldowns.getOrDefault(key, 0L) - currentTick);
    }
}
