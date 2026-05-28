# Machine Guide

The machine stack combines descriptors, processing recipes, inventory routing, side-aware energy/fluid storage, generated UI bindings, and optional loader bridges.

## Machine Definition

```java
NexusMachineDefinition definition = NexusMachines.register(NexusMachines.machine(MOD_ID, "ruby_press")
        .category("gem_processing")
        .energy(10_000, 250, 250)
        .fluid(4_000)
        .slots("input", SlotRole.INPUT, 0, 1)
        .slots("output", SlotRole.OUTPUT, 1, 2)
        .slots("upgrades", SlotRole.UPGRADE, 2, 4)
        .build());
```

The definition includes:

- ID and category.
- Energy capacity, max input, max output.
- Fluid capacity.
- Inventory size and named slot groups.
- Side configuration.
- Upgrade descriptors.
- Generated screen flag.
- Comparator output flag.
- Default redstone mode.

## Recipe Definition

```java
MachineRecipeDefinition recipe = MachineRecipeDefinition.builder(id("ruby_pressing"), id("ruby_press"))
        .input(new ItemStack(Items.REDSTONE))
        .fluidInput(new FluidStack(Fluids.WATER, 250))
        .output(new ItemStack(Items.DIAMOND))
        .energy(100)
        .ticks(40)
        .category("gem_processing")
        .group("ruby")
        .build();
```

Recipes are regular descriptors. You can keep them in code, generate them, or adapt custom datapack data into them.

## Processing Engine

```java
MachineProcessingEngine engine = new MachineProcessingEngine(definition, inventory, energy, tank, state, random);
engine.tick(List.of(recipe), false);
```

The engine:

- Finds matching recipes.
- Simulates item/fluid/energy requirements.
- Advances progress.
- Consumes inputs.
- Produces outputs.
- Records stall reasons in `MachineState`.

Use `simulate=true` for previews and tests.

## Inventory

`SimpleItemHandler` is the storage implementation. `SlotGroup`, `SlotRange`, and `SlotRole` describe semantics. `QuickMoveRouter` handles shift-click style movement:

```java
new QuickMoveRouter()
        .route(new SlotRange(0, 1), new SlotRange(1, 37), stack -> true)
        .route(new SlotRange(1, 37), new SlotRange(0, 1), stack -> true);
```

For richer automation rules:

```java
TransferRule rule = TransferRule.builder("input_to_output", new SlotRange(0, 1), new SlotRange(1, 4))
        .maxPerOperation(16)
        .automation(true)
        .player(true)
        .hopper(false)
        .build();

TransferResult result = InventoryTransfer.route(inventory, 0, TransferRule.TransferActor.PLAYER, List.of(rule), false);
```

Use `InventorySnapshot.capture` and `diff` in tests and debug reports.

## Energy

```java
NexusEnergyStorage energy = NexusEnergyStorage.builder(10_000)
        .io(250, 250)
        .side(Direction.NORTH, EnergyAccess.INPUT)
        .side(Direction.SOUTH, EnergyAccess.OUTPUT)
        .build();
```

Move energy between storages:

```java
NexusEnergyTransfer.Result result = NexusEnergyTransfer.move(source, Direction.SOUTH, target, Direction.NORTH, 200, false);
```

Fabric exposes this through Team Reborn Energy. NeoForge exposes it through capabilities.

## Fluids

```java
NexusFluidTank tank = NexusFluidTank.builder(4_000)
        .side(Direction.NORTH, FluidAccess.INPUT)
        .side(Direction.SOUTH, FluidAccess.OUTPUT)
        .build();
```

Move fluids:

```java
NexusFluidTransfer.Result result = NexusFluidTransfer.move(source, Direction.SOUTH, target, Direction.NORTH, 250, false);
```

Fabric exposes this through Fabric Transfer. NeoForge exposes it through fluid capabilities.

## Generated Machine UI

`MachineScreenLayout.generated(definition)` creates widget descriptors for:

- Progress arrow
- Energy bar
- Fluid tank
- Redstone mode
- Side configuration

`MachineUiBindings.machine(state, energy, tank)` exposes observable values:

- `machine.progress`
- `machine.status`
- `machine.redstone`
- `machine.energy`
- `machine.energy.capacity`
- `machine.fluid.amount`
- `machine.fluid.capacity`

`NexusMachineScreen` renders these descriptors with owo on the client.

## Block Entities

`BaseMachineBlockEntity` is the shared stateful base when a machine needs an actual world block entity. Use `NexusBlockEntities.blockEntity` to register block entity types and `NexusBlockEntities.setChangedAndSync` after state changes.

## Example

The scaffolded coverage ruby press demonstrates:

- Descriptor registration.
- Hybrid item/fluid/energy recipe.
- Processing engine tick loop.
- Generated screen layout.
- Machine UI bindings.
- Transfer rules.
- Runtime validation.
- Debug state.

See `docs/scaffolding-walkthrough.md` and `NexusCoreGameTestScenarios`.
