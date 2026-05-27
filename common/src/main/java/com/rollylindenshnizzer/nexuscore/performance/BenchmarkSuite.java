package com.rollylindenshnizzer.nexuscore.performance;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class BenchmarkSuite {
    private final List<BenchmarkCase> cases = new ArrayList<>();

    public BenchmarkSuite add(BenchmarkCase benchmark) {
        cases.add(benchmark);
        return this;
    }

    public BenchmarkSuite add(String name, int iterations, Runnable body) {
        return add(BenchmarkCase.of(name, iterations, body));
    }

    public List<BenchmarkCase> cases() {
        return List.copyOf(cases);
    }

    public List<BenchmarkResult> run() {
        List<BenchmarkResult> results = new ArrayList<>();
        for (BenchmarkCase benchmark : cases) {
            results.add(run(benchmark));
        }
        return results;
    }

    public static BenchmarkResult run(BenchmarkCase benchmark) {
        for (int i = 0; i < benchmark.warmupIterations(); i++) {
            benchmark.body().run();
        }
        long total = 0;
        long min = Long.MAX_VALUE;
        long max = 0;
        for (int i = 0; i < benchmark.iterations(); i++) {
            long start = System.nanoTime();
            benchmark.body().run();
            long elapsed = System.nanoTime() - start;
            total += elapsed;
            min = Math.min(min, elapsed);
            max = Math.max(max, elapsed);
            NamedProfiler.global().record("benchmark:" + benchmark.name(), elapsed);
        }
        return new BenchmarkResult(benchmark.name(), benchmark.warmupIterations(), benchmark.iterations(),
                total, min == Long.MAX_VALUE ? 0 : min, max);
    }

    public static String toMarkdown(List<BenchmarkResult> results) {
        StringBuilder builder = new StringBuilder("| Benchmark | Iterations | Total ms | Avg us | Min us | Max us |\n");
        builder.append("| --- | ---: | ---: | ---: | ---: | ---: |\n");
        for (BenchmarkResult result : results) {
            builder.append("| ").append(result.name())
                    .append(" | ").append(result.iterations())
                    .append(" | ").append(format(result.totalMillis()))
                    .append(" | ").append(format(result.averageMicros()))
                    .append(" | ").append(format(result.minMicros()))
                    .append(" | ").append(format(result.maxMicros()))
                    .append(" |\n");
        }
        return builder.toString();
    }

    private static String format(double value) {
        return String.format(Locale.ROOT, "%.3f", value);
    }
}
