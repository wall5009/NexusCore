# Debugging Guide

NexusCore treats diagnostics as a first-class API. Each subsystem should expose enough state to explain what was registered, what generated, what loaded, and what failed.

## Debug Registry

Register short live sections:

```java
DebugRegistry.section("example.machine", () -> state.status() + " progress=" + state.progressFraction());
DebugRegistry.section("example.resources", () -> resourceReport.summary());
```

Good debug sections are:

- Cheap to compute.
- Side-safe.
- Short enough for commands.
- Stable enough for support reports.

The scaffolding and GameTest coverage registers sections for items, blocks, components, config, machine state, validation, systems, datapack loading, benchmarks, registry reports, menus, and entities.

## Debug Commands

Install debug commands for a mod:

```java
NexusDebugCommands.install(MOD_ID);
```

Register a simple command:

```java
NexusCommands.literal("example")
        .permission(0)
        .feedback(Component.literal("Example command registered"))
        .register();
```

`NexusCoreCommands.install()` installs the core `/nexus` diagnostics.

## Doctor Reports

`NexusDoctor` can check:

- Module alignment.
- Content manifest entries.
- Datagen validation.
- Config dependency graphs.
- Packet declarations.
- Migration diagnostics.

```java
DoctorReport report = NexusDoctor.create(MOD_ID)
        .checkModules()
        .checkContent()
        .checkDatagen()
        .checkConfigs()
        .checkPackets()
        .checkMigrations()
        .run();
```

Write reports with `writeReportsTo(path)` for release artifacts.

## Datagen Reports

```java
DataValidationReport report = NexusDataValidator.validatePlan(plan);
DatagenReportWriters.writeMarkdown(report, output.resolve("datagen.md"));
```

Warnings should be reviewed before release. Errors should block release.

## Crash Hints

`CrashHints` and `CrashHintClassifier` turn common failures into targeted guidance:

- Client class loaded on dedicated server.
- Registry object accessed too early.
- Codec field errors.
- Packet/network failures.

Register project-specific hints:

```java
CrashHints.register("example_missing_texture", "Check generated model paths.");
```

## Event Trace

`EventTrace` keeps a bounded list of timestamped events:

```java
EventTrace.record("machine", "recipe started");
EventTrace.maxEntries(512);
```

Use this for high-level breadcrumbs, not per-tick spam.

## Profiler HUD

`NamedProfiler.global()` records named sections:

```java
try (NamedProfiler.Section ignored = NamedProfiler.global().section("example.machine_tick")) {
    engine.tick(recipes, false);
}
```

On the client, the Nexus profiler HUD can display top sections.

## Example

`NexusCoreGameTestScenarios` demonstrates crash hints, doctor reports, datagen validation summaries, registry reports, debug sections, event traces, and profiler data.
