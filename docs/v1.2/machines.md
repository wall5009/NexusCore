# Machines In v1.2

v1.2 introduces a complete common machine stack: descriptors, recipes, processing, state, inventory transfer, energy/fluid transfer, generated machine layouts, and a concrete owo machine screen runtime.

## Descriptor

`NexusMachineDefinition` is the contract that other systems read:

```java
NexusMachineDefinition press = NexusMachines.register(NexusMachines.machine(MOD_ID, "ruby_press")
        .category("gem_processing")
        .energy(10_000, 250, 250)
        .fluid(4_000)
        .slots("input", SlotRole.INPUT, 0, 1)
        .slots("output", SlotRole.OUTPUT, 1, 2)
        .slots("upgrades", SlotRole.UPGRADE, 2, 4)
        .side(Direction.NORTH, SideConfiguration.INPUT)
        .side(Direction.SOUTH, SideConfiguration.OUTPUT)
        .redstone(RedstoneControlMode.IGNORED)
        .build());
```

Use named slot groups rather than hardcoding slot math throughout menus and block entities.

## Recipe

`MachineRecipeDefinition` is a portable processing recipe:

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

The processing engine can consume recipes from code or from your own `TypedDataLoader` output.

## State

`MachineState` stores:

- Current status.
- Progress.
- Stall reason.
- Redstone control mode.
- Side configuration.

Use this as the single state object for debug, UI bindings, block entity save/load, and processing.

## Processing Engine

```java
MachineProcessingEngine engine = new MachineProcessingEngine(definition, inventory, energy, tank, state, random);
MachineProcessResult result = engine.tick(List.of(recipe), false);
```

The engine checks:

- Matching input.
- Available output space.
- Available energy.
- Available fluid.
- Redstone state.
- Upgrade modifiers.

It then consumes resources and produces output when the timer completes.

## Generated Layout

```java
MachineScreenLayout layout = MachineScreenLayout.generated(definition);
```

Generated widgets include:

- `progress_arrow`
- `energy_bar`
- `fluid_tank`
- `redstone_mode`
- `side_config`

Custom layouts can add `WidgetDescriptor` entries for registry pickers, tag pickers, color swatches, key/value tables, and custom widgets.

## owo Runtime Screen

`NexusMachineScreen` renders machine layouts end-to-end with owo components. It supports:

- Progress arrows.
- Energy bars.
- Fluid tanks.
- Redstone mode controls.
- Side configuration controls.
- Item slot groups.
- Inventory previews.
- Labels, text, and buttons.
- Fallback custom widget labels.

`NexusMachineScreens.open(...)` opens a concrete screen. `openFirstPreview(...)` is used by the debug browser to preview the first registered machine with dummy state.

## Debug And Testing

Expose:

```java
DebugRegistry.section("example.machine", () -> state.status() + " output=" + inventory.get(1).getCount());
```

Test:

- Matching recipe produces output.
- Missing energy stalls.
- Missing fluid stalls.
- Full output stalls.
- Redstone settings are respected.
- Side IO allows/blocks transfer as expected.

The production GameTests cover hybrid recipe processing, inventory transfer traces, and side-aware energy/fluid transfer.

## Example

See the ruby press in `scaffolded content` and the UI/binding coverage in `NexusCoreGameTestScenarios`.
