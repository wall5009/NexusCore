package com.rollylindenshnizzer.nexuscore.performance;

public record BenchmarkResult(String name, int warmupIterations, int iterations, long totalNanos,
                              long minNanos, long maxNanos) {
    public double totalMillis() {
        return totalNanos / 1_000_000.0;
    }

    public double averageMicros() {
        return iterations == 0 ? 0 : totalNanos / 1_000.0 / iterations;
    }

    public double minMicros() {
        return minNanos / 1_000.0;
    }

    public double maxMicros() {
        return maxNanos / 1_000.0;
    }
}
