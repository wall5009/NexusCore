# Testing, Diagnostics, And Gradle In v1.2

v1.2 strengthens release confidence through expanded GameTests, validation reports, benchmarks, profiler HUD support, Gradle TestKit tests, generated docs, ABI checks, and release metadata.

## GameTests

Fabric and NeoForge run the same shared scenarios from `NexusCoreGameTestScenarios`.

Current scenarios cover:

- Energy storage rate limits.
- Config schema export and synced option metadata.
- Datagen validation failures and valid plans.
- Packet diagnostics and version comparisons.
- Recipe viewer controls and portable fallbacks.
- Benchmark/profiler recording.
- Machine processing with hybrid item/fluid/energy recipes.
- Inventory transfer traces.
- Side-aware energy and fluid transfer.
- Worldgen and entity descriptor generation.
- Typed datapack loader validation.

Run:

```powershell
.\gradlew.bat :fabric:runGametest
.\gradlew.bat :neoforge:runGametest
```

## Diagnostics

Use:

- `DebugRegistry` for live sections.
- `NexusDoctor` for module/content/datagen/config/packet/migration reports.
- `NexusDataValidator` for generated assets/data.
- `NexusRegistryReports` for registry counts.
- `CrashHintClassifier` for support-friendly crash guidance.
- `EventTrace` for bounded runtime breadcrumbs.
- `NamedProfiler` and `NexusProfilerHud` for client-visible profiler data.

## Benchmarks

```java
List<BenchmarkResult> results = new BenchmarkSuite()
        .add("machine-cache", 1_000, cache::lookup)
        .run();
```

Registered benchmarks can be run through `NexusBenchmarks.runRegistered`. Benchmark output can be turned into markdown with `BenchmarkSuite.toMarkdown`.

## Gradle Plugin

The Gradle plugin provides tasks and templates for:

- Machine scaffolds.
- Transfer scaffolds.
- Worldgen scaffolds.
- Entity scaffolds.
- Projectile scaffolds.
- Datapack loader scaffolds.
- Validation.
- Release metadata.
- Docs site generation.
- Binary compatibility.

Functional tests use Gradle TestKit in `nexus-gradle/src/test`.

## ABI Compatibility

Release mode must pass:

```powershell
.\gradlew.bat checkBinaryCompatibility -PnexusApiBaseline=common/build/libs/nexuscore-common-1.0.0-build.3.jar -PnexusRelease=true
```

JApiCmp is the release gate. Generated API reports are supplementary, not a replacement.

## Architectury Transformer

NexusCore pins Architectury Transformer to `5.2.91` because older `5.2.87` emitted a post-success shutdown exception after GameTest completion. Fabric and NeoForge GameTests should exit without that post-success trace.

Generated `.architectury-transformer` output is ignored and should not be committed.

## Example

The example mod exposes validation, packet tests, benchmark output, registry reports, datapack loader summaries, and subsystem snapshots through debug sections. It is intended to be a living release smoke test for the public API.
