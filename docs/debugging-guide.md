# Debugging Guide

NexusCore includes diagnostics, debug commands, report export, and an owo-powered debug browser.

## Startup Diagnostics

`NexusDiagnostics.startup(modId)` reports environment, lifecycle, and registry information. `NexusCore.init()` and `NexusMod.init()` log startup diagnostics automatically.

## Debug Registry

Register live sections:

```java
DebugRegistry.section("example.energy", () -> Long.toString(storage.amount()));
DebugRegistry.section("example.mode", () -> mode.name());
```

Sections are read lazily, so values can reflect runtime state.

## Commands

Install the built-in debug command tree:

```java
NexusDebugCommands.install(MOD_ID);
```

Use `NexusCommands.literal(...)` for regular commands:

```java
NexusCommands.literal("example")
        .permission(0)
        .feedback(Component.literal("Example command registered"))
        .register();
```

## Debug UI And owo-lib

The UI module does properly depend on owo-lib. `NexusDebugScreen` imports owo UI classes directly, common compiles against owo-lib, and both loader modules require owo at runtime. The client entrypoint initializes `NexusCoreClient`, which registers the F9 debug browser. The screen reads `DebugRegistry` sections and presents them through owo components.

## Reports

`ReportExporter` can be used to serialize debug output for issue reports. Prefer adding high-signal sections to `DebugRegistry` instead of logging large dumps every tick.
