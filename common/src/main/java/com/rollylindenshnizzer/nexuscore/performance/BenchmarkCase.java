package com.rollylindenshnizzer.nexuscore.performance;

import java.util.Objects;

public record BenchmarkCase(String name, int warmupIterations, int iterations, Runnable body) {
    public BenchmarkCase {
        name = Objects.requireNonNull(name, "name");
        body = Objects.requireNonNull(body, "body");
        warmupIterations = Math.max(0, warmupIterations);
        iterations = Math.max(1, iterations);
    }

    public static BenchmarkCase of(String name, int iterations, Runnable body) {
        return new BenchmarkCase(name, Math.min(100, Math.max(0, iterations / 10)), iterations, body);
    }
}
