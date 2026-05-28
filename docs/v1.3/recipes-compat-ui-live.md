# Recipes, Compatibility, UI, and Live Reload

## Recipe Families

`NexusRecipeFamilies` groups advanced processing recipes by machine family. A recipe can include item inputs, fluid inputs, item outputs, fluid outputs, chance outputs, energy, ticks, and named conditions.

Use guide indexes to connect recipes to in-game documentation and recipe viewer categories:

```java
NexusRecipeFamilies.family("yourmod", "alloying")
        .category("alloying")
        .recipe(recipe)
        .guide(new RecipeGuideIndex(List.of("Alloying"), List.of("alloy", "machine")))
        .build();
```

## Compatibility

`NexusCompatibility.integration` records optional integrations for JEI, EMI, REI, tooltip providers, equipment APIs, permissions, claims, and mapping docs. `CompatibilityMatrix` gives diagnostics and docs a single place to render active and inactive integrations.

## UI and Debug Workbench

v1.3 builds on v1.2 owo config screens, machine UI descriptors, recipe viewer layouts, HUD overlays, and debug browser work. Register new tabs with `NexusDebugWorkbench.register` and record reports with `NexusDebugWorkbench.record`.

## Live Reload

`NexusLiveReload.watch` registers safe watch specs. `ReloadSafety.developmentOnly` blocks production reloads; `productionSafe` allows limited reloads with permission checks. Live reload is intentionally safety-gated and should not be used as a no-code production editor.

