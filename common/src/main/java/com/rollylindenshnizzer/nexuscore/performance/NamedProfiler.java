package com.rollylindenshnizzer.nexuscore.performance;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class NamedProfiler {
    private static final NamedProfiler GLOBAL = new NamedProfiler();

    private final Map<String, Stat> stats = new ConcurrentHashMap<>();

    public static NamedProfiler global() {
        return GLOBAL;
    }

    public Section section(String name) {
        return new Section(name, System.nanoTime());
    }

    public Map<String, Stat> snapshot() {
        return Map.copyOf(new LinkedHashMap<>(stats));
    }

    public List<Map.Entry<String, Stat>> top(int count) {
        return stats.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.comparingLong(Stat::nanos).reversed()))
                .limit(Math.max(0, count))
                .toList();
    }

    public void record(String name, long nanos) {
        stats.merge(name, new Stat(1, nanos, nanos, nanos), Stat::merge);
    }

    public void reset() {
        stats.clear();
    }

    public final class Section implements AutoCloseable {
        private final String name;
        private final long start;

        private Section(String name, long start) {
            this.name = name;
            this.start = start;
        }

        @Override
        public void close() {
            long elapsed = System.nanoTime() - start;
            record(name, elapsed);
        }
    }

    public record Stat(long calls, long nanos, long minNanos, long maxNanos) {
        private Stat merge(Stat other) {
            return new Stat(calls + other.calls, nanos + other.nanos,
                    Math.min(minNanos, other.minNanos), Math.max(maxNanos, other.maxNanos));
        }

        public double millis() {
            return nanos / 1_000_000.0;
        }

        public double averageMillis() {
            return calls == 0 ? 0 : millis() / calls;
        }

        public double minMillis() {
            return minNanos / 1_000_000.0;
        }

        public double maxMillis() {
            return maxNanos / 1_000_000.0;
        }
    }
}
