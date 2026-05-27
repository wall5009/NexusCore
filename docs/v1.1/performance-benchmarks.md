# Performance Benchmarks and HUD

Use `BenchmarkSuite` for repeatable local benchmarks:

```java
var results = new BenchmarkSuite()
    .add("cache lookup", 10_000, () -> cache.get("key"))
    .run();
```

`BenchmarkSuite.toMarkdown(results)` converts results into CI-friendly tables.

For runtime profiling, wrap hot paths:

```java
try (var ignored = NamedProfiler.global().section("my_mod.machine_tick")) {
    tickMachine();
}
```

Press `F10` in-game to toggle the Nexus profiler HUD. The HUD renders the top sampled sections by total time and average duration.

The GameTest suite runs the smoke benchmark suite to make sure the profiler and benchmark registry stay functional on both Fabric and NeoForge.
