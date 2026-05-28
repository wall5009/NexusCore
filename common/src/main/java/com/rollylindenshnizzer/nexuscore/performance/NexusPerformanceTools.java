package com.rollylindenshnizzer.nexuscore.performance;

import com.rollylindenshnizzer.nexuscore.api.NexusStable;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@NexusStable(since = "1.3")
public final class NexusPerformanceTools {
    public static <K, V> GraphCache<K, V> graphCache(String name, int maxEntries) {
        return new GraphCache<>(name, maxEntries);
    }

    public static PerformanceDiff diff(PerformanceSnapshot baseline, PerformanceSnapshot current) {
        Map<String, Double> deltas = new LinkedHashMap<>();
        current.metrics().forEach((key, value) -> deltas.put(key, value - baseline.metrics().getOrDefault(key, 0.0D)));
        return new PerformanceDiff(baseline.name(), current.name(), deltas);
    }

    public static MemoryTracker memoryTracker() {
        return new MemoryTracker(new LinkedHashMap<>(), List.of());
    }

    @NexusStable(since = "1.3")
    public static final class GraphCache<K, V> {
        private final String name;
        private final int maxEntries;
        private final LinkedHashMap<K, V> values = new LinkedHashMap<>();

        private GraphCache(String name, int maxEntries) {
            this.name = name;
            this.maxEntries = Math.max(1, maxEntries);
        }

        public void put(K key, V value) {
            values.put(key, value);
            while (values.size() > maxEntries) {
                K first = values.keySet().iterator().next();
                values.remove(first);
            }
        }

        public Optional<V> get(K key) {
            return Optional.ofNullable(values.get(key));
        }

        public CacheDiagnostics diagnostics() {
            return new CacheDiagnostics(name, values.size(), maxEntries);
        }
    }

    @NexusStable(since = "1.3")
    public record CacheDiagnostics(String name, int size, int maxEntries) {
    }

    @NexusStable(since = "1.3")
    public record PerformanceSnapshot(String name, Instant createdAt, Map<String, Double> metrics) {
        public PerformanceSnapshot {
            metrics = Map.copyOf(metrics);
        }

        public PerformanceSnapshot metric(String key, double value) {
            Map<String, Double> values = new LinkedHashMap<>(metrics);
            values.put(key, value);
            return new PerformanceSnapshot(name, createdAt, values);
        }
    }

    @NexusStable(since = "1.3")
    public record PerformanceDiff(String baseline, String current, Map<String, Double> deltas) {
        public PerformanceDiff {
            deltas = Map.copyOf(deltas);
        }
    }

    @NexusStable(since = "1.3")
    public record PerformanceBudget(String name, Map<String, Double> maxValues) {
        public PerformanceBudget {
            maxValues = Map.copyOf(maxValues);
        }

        public List<String> check(PerformanceSnapshot snapshot) {
            return maxValues.entrySet().stream()
                    .filter(entry -> snapshot.metrics().getOrDefault(entry.getKey(), 0.0D) > entry.getValue())
                    .map(entry -> entry.getKey() + " exceeds " + entry.getValue())
                    .toList();
        }
    }

    @NexusStable(since = "1.3")
    public record MemoryTracker(Map<String, Integer> counts, List<String> leakWarnings) {
        public MemoryTracker {
            counts = Map.copyOf(counts);
            leakWarnings = List.copyOf(leakWarnings);
        }

        public MemoryTracker track(String category, int count) {
            Map<String, Integer> values = new LinkedHashMap<>(counts);
            values.put(category, count);
            List<String> warnings = new ArrayList<>(leakWarnings);
            if (count > 10_000) {
                warnings.add(category + " has a very large cache count: " + count);
            }
            return new MemoryTracker(values, warnings);
        }

        public MemoryTracker cachedDefinitions(int count) {
            return track("cached_definitions", count);
        }

        public MemoryTracker activeRituals(int count) {
            return track("active_rituals", count);
        }

        public MemoryTracker multiblockCaches(int count) {
            return track("multiblock_caches", count);
        }

        public MemoryTracker automationNetworks(int count) {
            return track("automation_networks", count);
        }

        public MemoryTracker generatedPreviews(int count) {
            return track("generated_previews", count);
        }
    }

    @NexusStable(since = "1.3")
    public record PerformanceDashboard(List<PerformanceSnapshot> snapshots,
                                       List<PerformanceDiff> diffs,
                                       List<String> recommendedFixes) {
        public PerformanceDashboard {
            snapshots = List.copyOf(snapshots);
            diffs = List.copyOf(diffs);
            recommendedFixes = List.copyOf(recommendedFixes);
        }

        public static PerformanceDashboard from(Collection<PerformanceSnapshot> snapshots) {
            List<PerformanceSnapshot> values = List.copyOf(snapshots);
            List<PerformanceDiff> diffs = new ArrayList<>();
            for (int i = 1; i < values.size(); i++) {
                diffs.add(NexusPerformanceTools.diff(values.get(i - 1), values.get(i)));
            }
            List<String> fixes = values.stream()
                    .flatMap(snapshot -> snapshot.metrics().entrySet().stream())
                    .filter(entry -> entry.getKey().contains("slow") && entry.getValue() > 0.0D)
                    .map(entry -> "Review " + entry.getKey())
                    .distinct()
                    .toList();
            return new PerformanceDashboard(values, diffs, fixes);
        }
    }

    private NexusPerformanceTools() {
    }
}
