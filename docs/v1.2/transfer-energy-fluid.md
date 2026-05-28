# Transfer, Energy, And Fluid In v1.2

v1.2 adds side-aware transfer primitives that common machine code can use directly and loader bridges can expose to Fabric and NeoForge.

## Inventory Transfer

Use `SimpleItemHandler` as a compact item storage:

```java
SimpleItemHandler inventory = new SimpleItemHandler(4);
inventory.set(0, new ItemStack(Items.REDSTONE, 8));
```

Describe movement with `TransferRule`:

```java
TransferRule rule = TransferRule.builder("input_to_output", new SlotRange(0, 1), new SlotRange(1, 4))
        .maxPerOperation(4)
        .automation(true)
        .player(true)
        .hopper(false)
        .build();

TransferResult result = InventoryTransfer.route(inventory, 0, TransferRule.TransferActor.PLAYER, List.of(rule), false);
```

Capture and diff state:

```java
InventorySnapshot before = InventorySnapshot.capture(inventory);
// mutate inventory
List<InventorySnapshot.Change> changes = before.diff(InventorySnapshot.capture(inventory));
```

Use `InventoryDropPolicy` to document what should happen when a menu closes, block breaks, or storage migrates.

## Energy

```java
NexusEnergyStorage storage = NexusEnergyStorage.builder(10_000)
        .io(250, 250)
        .side(Direction.NORTH, EnergyAccess.INPUT)
        .side(Direction.SOUTH, EnergyAccess.OUTPUT)
        .build();
```

Move between storages:

```java
NexusEnergyTransfer.Result result = NexusEnergyTransfer.move(source, Direction.SOUTH, target, Direction.NORTH, 200, false);
```

Result traces explain blocked sides or moved amounts.

## Fluid

```java
NexusFluidTank tank = NexusFluidTank.builder(4_000)
        .side(Direction.NORTH, FluidAccess.INPUT)
        .side(Direction.SOUTH, FluidAccess.OUTPUT)
        .build();

tank.fill(new FluidStack(Fluids.WATER, 1_000), false);
```

Move between tanks:

```java
NexusFluidTransfer.Result result = NexusFluidTransfer.move(source, Direction.SOUTH, target, Direction.NORTH, 250, false);
```

## Loader Bridges

Fabric:

- Item/fluid transfer uses Fabric Transfer API.
- Energy uses Team Reborn Energy.

NeoForge:

- Item/fluid/energy transfer uses NeoForge capabilities.

Common code should not depend on loader capability classes. Keep common logic on `SimpleItemHandler`, `NexusEnergyStorage`, and `NexusFluidTank`.

## Example

`NexusCoreGameTestScenarios` moves energy and fluid between side-aware storages. `demonstrateInventory` routes item stacks with transfer rules and records an inventory diff.
