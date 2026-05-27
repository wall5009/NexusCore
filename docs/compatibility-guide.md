# Compatibility Guide

NexusCore keeps optional integrations optional while still providing concrete runtime bridges when the integration is present.

## Recipe Viewers

The shared recipe viewer API is in `compat.recipeviewer`. It defines portable categories, displays, pages, slots, text, tooltips, fluids, animated progress, and advanced controls.

Shared registration:

```java
RecipeViewerBridge.category(new RecipeViewerCategory(id("ruby_workbench"),
        Component.translatable("recipe_viewer.example.ruby_workbench"),
        new ItemStack(ruby.get()), 132, 72, new ItemStack(rubyBlock.get())));

RecipeViewerBridge.display(RecipeViewerDisplay.builder(id("ruby_preview"), id("ruby_workbench"), 132, 72)
        .page("main", page -> page
                .itemInput(6, 20, new ItemStack(rawRuby.get()))
                .fluidInput(28, 12, 12, 36, 4_000, new FluidStack(Fluids.WATER, 1_000))
                .arrowProgress(52, 20, 2_000)
                .itemOutput(92, 20, new ItemStack(ruby.get()))
                .jeiTransferButton(98, 50)
                .viewerControl("emi", "recipe_tree", Map.of("enabled", "true")))
        .build());
```

Concrete plugins exist for:

- JEI
- EMI
- REI

Viewer differences:

- Item slots, text, basic tooltips, and progress are portable.
- Fluid rendering is supported through each viewer's best available API.
- Transfer buttons and recipe trees are viewer-specific.
- Multi-page displays are portable at the NexusCore level; each plugin adapts presentation to the target viewer.

## owo-lib

owo-lib powers:

- Generated config editor screens.
- Debug browser screens.
- Machine screens.
- owo component layout helpers.

Common config definitions and descriptors remain server-safe. Client screens live in client packages and are loaded only by client entrypoints or reflective bridges.

## Fabric Transfer And Energy

Fabric fluid/item transfer uses Fabric Transfer API. Fabric energy uses Team Reborn Energy because Fabric API does not provide an official energy API.

Design guidance:

- Keep common machines on `NexusEnergyStorage`, `NexusFluidTank`, and `SimpleItemHandler`.
- Expose storage through loader bridges in the Fabric module.
- Avoid hard dependencies on Team Reborn Energy from common code.

## NeoForge Capabilities

NeoForge uses capabilities for item, fluid, and energy access. Common storage stays the same; the NeoForge module adapts it.

Design guidance:

- Keep side-aware behavior in common storage.
- Register capability providers on NeoForge block entities or items.
- Validate side semantics with GameTests where possible.

## Safe Classloading

Use `SafeClassloading` and loader metadata for optional integrations. Do not directly import optional mod classes from always-loaded common paths unless those classes are compile-only and never loaded without guards.

Common pattern:

```java
if (NexusEnvironment.isModLoaded("emi")) {
    // Register EMI-only adapter from a guarded integration class.
}
```

## Client-Only Code

Common code may register `ClientDescriptor` data because it contains only IDs and primitives. Actual renderers, key mappings, HUD draw callbacks, and screens belong in client runtime classes.

If common code must request a client object, use a reflective bridge such as `OwoConfigBridge.createGeneratedScreen`.

## Metadata

Declare optional integrations in loader metadata:

- Fabric `suggests`: JEI, EMI, REI, Team Reborn Energy.
- NeoForge optional dependencies: JEI, EMI, REI.
- Required dependencies: Minecraft, Java, Architectury, owo-lib, NexusCore.

## Example

The example mod demonstrates recipe viewer categories/displays, optional dependency metadata, owo-backed generated screens through config metadata, Fabric/NeoForge loader wrappers, Team Reborn Energy suggestion, and common-safe client descriptors.
