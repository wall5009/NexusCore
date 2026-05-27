package com.rollylindenshnizzer.nexuscore.performance;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public final class NexusBenchmarks {
    private static final List<BenchmarkCase> REGISTERED = new ArrayList<>();

    public static void register(BenchmarkCase benchmark) {
        REGISTERED.add(benchmark);
    }

    public static List<BenchmarkCase> registered() {
        return List.copyOf(REGISTERED);
    }

    public static List<BenchmarkResult> runRegistered() {
        BenchmarkSuite suite = new BenchmarkSuite();
        REGISTERED.forEach(suite::add);
        return suite.run();
    }

    public static BenchmarkSuite smokeSuite() {
        return new BenchmarkSuite()
                .add("profiler-section", 1_000, () -> {
                    try (NamedProfiler.Section ignored = NamedProfiler.global().section("benchmark.smoke")) {
                        Math.sqrt(144.0);
                    }
                })
                .add("rate-limiter", 1_000, () -> new NexusRateLimiter(Duration.ofMillis(20)).tryAcquire("smoke"));
    }

    private NexusBenchmarks() {
    }
}
