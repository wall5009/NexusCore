# Compatibility Guide

NexusCore exposes optional integrations without making third-party mods required.

## Recipe Viewers

Register simple item-stack recipes in common code:

```java
RecipeViewerBridge.category(new RecipeViewerCategory(
        id("ruby_workbench"),
        Component.translatable("recipe_viewer.example.ruby_workbench"),
        new ItemStack(ruby.get()),
        116,
        54,
        new ItemStack(rubyBlock.get())
));

RecipeViewerBridge.display(new RecipeViewerDisplay(
        id("ruby_workbench_preview"),
        id("ruby_workbench"),
        List.of(new ItemStack(rawRuby.get())),
        List.of(new ItemStack(ruby.get())),
        List.of(new ItemStack(rubyBlock.get()))
));
```

NexusCore then publishes the same data to:

- JEI through `IModPlugin` and the Fabric `jei_mod_plugin` entrypoint.
- EMI through `EmiPlugin` and the Fabric `emi` entrypoint.
- REI through `REIClientPlugin`, Fabric `rei_client`, and NeoForge `@REIPluginClient`.

The common bridge stores categories, displays, catalysts, and workstations. Loader plugins translate them into each viewer's native category, display, recipe, catalyst, and workstation APIs.

For custom layouts, use the layout DSL instead of the stack-only constructor:

```java
RecipeViewerBridge.display(RecipeViewerDisplay.builder(
        id("ruby_workbench_preview"),
        id("ruby_workbench"),
        132,
        72
).page("infusion", page -> page
        .text(Component.literal("Ruby Infusion"), 42, 4, 0x404040, false)
        .itemInput(6, 20, new ItemStack(rawRuby.get()))
        .fluidInput(28, 12, 12, 36, 4_000, new FluidStack(Fluids.WATER, 1_000))
        .arrowProgress(52, 20, 2_000)
        .itemOutput(92, 20, new ItemStack(ruby.get()))
        .itemCatalyst(6, 50, new ItemStack(rubyBlock.get()))
        .tooltip(52, 20, 24, 17, List.of(Component.literal("Animated work progress")))
        .jeiTransferButton(98, 50)
        .viewerControl("emi", "recipe_tree", Map.of("enabled", "true"))
        .viewerControl("rei", "button", Map.of("x", "98", "y", "50", "width", "28", "height", "18", "text", "Info"))
).page("charging", page -> page
        .itemInput(12, 24, new ItemStack(ruby.get()))
        .arrowProgress(52, 24, 1_200)
        .itemOutput(92, 24, new ItemStack(rubyApple.get()))
).build());
```

The DSL supports:

- Item, fluid, catalyst, output, and render-only slots.
- Fluid tank dimensions and capacity.
- Animated progress widgets with built-in arrows or custom textures.
- Arbitrary text labels and hover tooltip regions.
- Multi-page displays. Each page is registered as a native display/recipe in JEI, EMI, and REI so all viewers can index it.
- Viewer-specific controls. Built-in controls include JEI recipe-transfer button relocation and shapeless markers, EMI recipe-tree and craftable visibility toggles, and generic button/tooltip controls for EMI and REI.

Viewer support is concrete:

- JEI maps slots to `IRecipeLayoutBuilder`, fluids through JEI fluid stacks/renderers, progress/text through category drawing, and tooltips through rich tooltip callbacks.
- EMI maps slots/tanks to `SlotWidget` and `TankWidget`, progress through filling arrows or animated textures, and controls through EMI buttons/tooltips and recipe-tree flags.
- REI maps slots and fluids to `EntryIngredient`, progress to arrows or drawable texture widgets, text to labels, and controls to REI buttons/tooltips.

## Energy And Fluid Bridges

Fabric energy uses Team Reborn Energy API:

```java
team.reborn.energy.api.EnergyStorage fabricEnergy =
        FabricTransferBridges.energy(new EnergyStorage(10_000));
```

Fabric fluids use Fabric Transfer:

```java
Storage<FluidVariant> fabricFluid = FabricTransferBridges.fluid(new FluidTank(4_000));
```

NeoForge uses native capabilities:

```java
IEnergyStorage energy = NeoForgeCapabilities.energy(new EnergyStorage(10_000));
IFluidHandler fluid = NeoForgeCapabilities.fluid(new FluidTank(4_000));
```

## Optional Dependency Policy

Optional integrations are compile-only in Gradle and optional/suggested in loader metadata. NexusCore loads without JEI, EMI, REI, or Team Reborn Energy present, and those mods discover the integration only when installed.
