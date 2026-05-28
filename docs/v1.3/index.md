# NexusCore v1.3 Guide

v1.3 is the worlds, automation, data-driven content, and advanced creation systems release. It adds stable descriptor APIs for dimensions, portals, structures, biome/environment control, AI goals, automation networks, data definition registries, recipe families, compatibility reports, balance simulation, hot reload safety, performance budgets, and server safety, plus incubating APIs for multiblocks, rituals, progression, authoring, and large-scale simulation.

## Release Shape

- `nexus-dimension`: dimension definitions, dimension type JSON, portal descriptors, teleport targets, safe arrival policies, validation, and datagen helpers.
- `nexus-structure`: structure descriptors, template metadata, placement rules, structure sets, stable jigsaw pool definitions, debug explanations, runtime pack emission, and validation.
- `nexus-world`: biome definitions, selectors, environment effects, modifier groups, and balance reports.
- `nexus-ai`: goal descriptors, common goal library, home/territory/patrol helpers, path cache, stuck detector, and debug reports.
- `nexus-automation`: item, energy, and fluid routing graphs, filters, route planning, simulation reports, and concrete generated automation blocks.
- `nexus-resource`: typed data definition registries, schema docs, reload reports, unsafe JSON guards, generated entities, generated worldgen JSON, and runtime pack support.
- `nexus-compat`: integration matrix reports for recipe viewers, tooltips, equipment APIs, permissions, claims, and mapping docs.
- `nexus-multiblock`: fixed and scalable multiblocks, part roles, assembly metadata, machine integration, UI previews, datagen, validation, and debug overlays.
- `nexus-ritual`: custom interaction definitions, requirements, ingredients, effects, lifecycle runtime, stability, safety, UI feedback, datagen, and timeline debugging.
- `nexus-progression`: unlock graphs, node scopes, dependency checks, guide visibility, recipe/ritual/machine unlocks, and runtime inspection.
- `nexus-authoring`: developer-only editor descriptors, capture/export helpers, approved output roots, read-only mode, and export logging.
- `nexus-simulation`: worldgen, economy, combat, balance diff, and dashboard descriptors.

The repository intentionally does not include an embedded example mod anymore. Use `docs/v1.3/cookbook.md`, `docs/scaffolding-walkthrough.md`, the Gradle scaffolding tasks, and `NexusCoreGameTestScenarios` as the maintained examples.

## Runtime Installation

`NexusMod.init()` now calls `NexusRuntimeContent.install(modId)` before `NexusRegistries.registerAll(modId)`. That installer registers generated blocks and entity types, writes all descriptor data to `NexusData.plan(modId)`, and makes the resulting assets/data available through the always-enabled runtime pack on Fabric and NeoForge.

## Validation

Run:

```powershell
.\gradlew.bat runNexusValidation generateReleaseMetadata
.\gradlew.bat :fabric:runGametest
.\gradlew.bat :neoforge:runGametest
```

`runNexusValidation` now writes v1.3 reports for dimensions, structures, AI, automation, data definitions, balance diffs, multiblocks, rituals, brain AI, progression, simulation, schema generation, guide drafts, dashboards, ABI, docs, and v1.2 source compatibility entry points.

See `docs/v1.3/advanced-creation-systems.md` for the multiblock, ritual, authoring, progression, and simulation APIs.
