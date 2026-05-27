package com.rollylindenshnizzer.nexuscore.network;

import net.minecraft.resources.ResourceLocation;

import java.util.LinkedHashMap;
import java.util.Map;

public final class NetworkMonitor {
    private static final Map<ResourceLocation, Stats> STATS = new LinkedHashMap<>();

    public static synchronized void record(ResourceLocation packet, int bytes, Throwable exception) {
        Stats stats = STATS.getOrDefault(packet, new Stats(0, 0, 0));
        STATS.put(packet, new Stats(stats.count() + 1, stats.bytes() + Math.max(0, bytes),
                stats.exceptions() + (exception == null ? 0 : 1)));
    }

    public static synchronized Map<ResourceLocation, Stats> snapshot() {
        return Map.copyOf(STATS);
    }

    public record Stats(long count, long bytes, long exceptions) {
        public double averageBytes() {
            return count == 0 ? 0.0 : bytes / (double) count;
        }
    }

    private NetworkMonitor() {
    }
}
