# Testing Guide

NexusCore uses layered verification: pure Java validation, packet round trips, datagen validation, benchmarks, GameTests, Gradle TestKit, ABI checks, and release metadata validation.

## Validation Suite

Use `ValidationSuite` for lightweight runtime checks:

```java
ValidationSuite.Result result = new ValidationSuite()
        .check("energy seeded", () -> {
            if (energy.amount() <= 0) {
                throw new AssertionError("Expected energy");
            }
        })
        .check("machine output", () -> {
            if (inventory.get(1).isEmpty()) {
                throw new AssertionError("Expected output");
            }
        })
        .run();
```

Expose the result through `DebugRegistry` so validation is visible in dev runs.

## Assertions And Fake Data

Use `NexusAssertions` and `FakeInventory` for pure logic tests. Keep recipe matching, transfer routing, config validation, and serialization tests outside full Minecraft runtime whenever possible.

## Packet Tests

```java
PacketTestHarness.assertRoundTrip(packet,
        (buffer, value) -> value.encode(buffer),
        MyPacket::decode);
```

Packet tests should cover:

- Encode/decode equality.
- Invalid payload rejection.
- Permission/distance/dimension guard behavior.
- Protocol version diagnostics.

## Datagen Validation

```java
DataValidationReport report = NexusDataValidator.validatePlan(plan);
if (report.hasErrors()) {
    throw new AssertionError(report.summary());
}
```

Run datagen for Fabric and NeoForge if generated output differs by loader.

## GameTests

Shared scenarios live in `NexusCoreGameTestScenarios`. Fabric and NeoForge entrypoints call the same scenarios so behavior remains loader-equivalent.

Run:

```powershell
.\gradlew.bat :fabric:runGametest
.\gradlew.bat :neoforge:runGametest
```

Current production GameTests cover energy, config schema, datagen validation, packet diagnostics, recipe viewer controls, performance benchmarks, machine processing, inventory transfer, energy/fluid transfer, worldgen/entity descriptors, and typed data loading.

## Golden Files

Use `GoldenFiles` for generated JSON or docs snapshots when exact output matters. Keep golden files focused; broad snapshots become noisy during harmless formatting changes.

## Benchmarks And Profiling

Use `BenchmarkSuite` for local microbenchmarks and `NamedProfiler` for runtime sections:

```java
List<BenchmarkResult> results = new BenchmarkSuite()
        .add("recipe-cache", 1_000, cache::lookup)
        .run();
```

Use benchmarks to detect regressions, not to prove absolute performance across machines.

## Gradle Plugin Tests

The `nexus-gradle` module uses Gradle TestKit to validate scaffolding and validation tasks in real temporary builds. Add TestKit coverage whenever a new Gradle task, template, or release gate is added.

## ABI Compatibility

Stable releases must pass binary compatibility checks:

```powershell
.\gradlew.bat checkBinaryCompatibility -PnexusApiBaseline=common/build/libs/nexuscore-common-1.0.0-build.3.jar -PnexusRelease=true
```

If JApiCmp reports a break against a stable baseline, either restore compatibility or intentionally defer the change to the next major release.

## Release Gate

Before publishing:

- `build`
- `runNexusValidation`
- `generateReleaseMetadata`
- Fabric and NeoForge datagen.
- Fabric and NeoForge GameTests.
- Gradle plugin tests.
- Binary compatibility check.
- Docs site generation.
- `git diff --check`.

## Example

The example mod uses `ValidationSuite`, `PacketTestHarness`, debug sections, datagen validation, benchmarks, and subsystem snapshots. NexusCore itself includes the loader GameTests.
