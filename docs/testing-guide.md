# Testing Guide

NexusCore provides local assertion helpers, validation suites, fake inventories, and registered GameTests.

## Validation Suites

Use `ValidationSuite` for small runtime checks:

```java
ValidationSuite.Result result = new ValidationSuite()
        .check("energy inserted", () -> {
            if (storage.insert(100, false) != 100) {
                throw new AssertionError("Expected 100 energy");
            }
        })
        .run();
```

This is useful for diagnostics, examples, and non-Minecraft unit-style checks.

## Assertions

`NexusAssertions` includes helpers for block and inventory state:

```java
NexusAssertions.block(level, pos, Blocks.DIAMOND_BLOCK);
NexusAssertions.contains(container, Items.DIAMOND);
```

## GameTest

The production GameTest suite is registered on both loaders:

- Fabric entrypoint: `fabric-gametest`.
- NeoForge registration: `RegisterGameTestsEvent`.

Both suites run the same energy/validation behavior and use bundled structure resources:

- Fabric SNBT: `data/nexuscore/gametest/structure/bootstrap.snbt`.
- Vanilla/NeoForge NBT: `data/nexuscore/structure/bootstrap.nbt`.

Run them with:

```powershell
.\gradlew.bat :fabric:runGametest
.\gradlew.bat :neoforge:runGametest
```

Fabric also writes a JUnit-style report to `fabric/build/reports/gametest/fabric.xml`.

## When To Add Tests

Add a validation suite for pure Java behavior. Add a GameTest when behavior depends on the server world, block entities, inventories, fluids, redstone, or ticking.
