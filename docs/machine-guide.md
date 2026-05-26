# Machine Guide

The machine package provides small reusable pieces for block entities, inventories, fluids, energy, and cached recipes.

## Energy

`EnergyStorage` is a simple long-based store with capacity, max insert, max extract, simulation support, NBT save/load, and clamped setters.

```java
EnergyStorage storage = new EnergyStorage(10_000, 250, 250);
long accepted = storage.insert(500, false);
long extracted = storage.extract(100, false);
```

Use loader bridges when exposing it:

- Fabric: `FabricTransferBridges.energy(storage)`.
- NeoForge: `NeoForgeCapabilities.energy(storage)`.

## Fluids

`FluidTank` stores one fluid type at a time.

```java
FluidTank tank = new FluidTank(4_000);
tank.fill(new FluidStack(Fluids.WATER, 1_000), false);
FluidStack drained = tank.drain(250, false);
```

Use `FabricTransferBridges.fluid(tank)` or `NeoForgeCapabilities.fluid(tank)` for platform exposure.

## Inventory

`SimpleItemHandler` gives a compact item storage. `SlotRange` and `QuickMoveRouter` describe shift-click routing without hardcoding slot math into menu code.

```java
QuickMoveRouter router = new QuickMoveRouter()
        .route(new SlotRange(0, 1), new SlotRange(1, 37), stack -> true)
        .route(new SlotRange(1, 37), new SlotRange(0, 1), stack -> true);
```

## Base Machines

`BaseMachineBlockEntity` tracks shared machine state. `MachineState`, `RedstoneControlMode`, `MachineRecipe`, and `MachineRecipeCache` cover common machine loops. Use them for simple processing machines, then subclass when you need custom side IO, animation, or recipe matching.
