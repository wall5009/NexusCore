package com.rollylindenshnizzer.nexuscore.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class NexusLoggers {
    private static final Set<String> WARNED_KEYS = ConcurrentHashMap.newKeySet();
    private static final Map<String, Long> RATE_LIMITS = new ConcurrentHashMap<>();

    public static Logger get(String modId) {
        return LoggerFactory.getLogger("NexusCore/" + NexusIds.requireNamespace(modId));
    }

    public static void debugOnly(Logger logger, String message, Object... args) {
        if (NexusEnvironment.isDevelopment()) {
            logger.debug(message, args);
        }
    }

    public static void warnOnce(Logger logger, String key, String message, Object... args) {
        if (WARNED_KEYS.add(key)) {
            logger.warn(message, args);
        }
    }

    public static void warnRateLimited(Logger logger, String key, Duration interval, String message, Object... args) {
        long now = System.currentTimeMillis();
        long last = RATE_LIMITS.getOrDefault(key, 0L);
        if (now - last >= interval.toMillis()) {
            RATE_LIMITS.put(key, now);
            logger.warn(message, args);
        }
    }

    private NexusLoggers() {
    }
}
