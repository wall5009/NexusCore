# v1.3 Advanced Creation Systems

NexusCore v1.3 now includes the advanced creation APIs that were planned under the v1.3 wave. There is no separate "Part 2" compatibility line in the repository: multiblocks, rituals, brain AI, authoring, progression, simulation, and the expanded data system are all documented and versioned as v1.3 features.

The embedded example mod remains removed. Examples live in docs, Gradle scaffolds, templates, and GameTest scenarios.

## Multiblocks

Use `NexusMultiblocks.create(id)` for fixed structures and `NexusMultiblocks.scalable(id)` for tanks, reactors, shells, portals, and other bounded scalable builds. Definitions support layered patterns, block/tag/state/predicate matchers, optional parts, rotatable and mirrorable layouts, controller metadata, part roles, validation reports, preview layers, datagen JSON, debug overlays, assembly events, server-authoritative safety metadata, cache limits, and machine integration descriptors.

## Rituals

Use `NexusRituals.create(id)` for custom world interactions. Rituals support center blocks, required structures, item/fluid/energy/entity inputs, weather, time, biome, dimension, permission, advancement, custom predicates, duration, delay, cooldown, manual and automatic starts, channeled/player-maintained flows, pause/cancel/complete/failure states, stability policies, safety caps, guide drafts, recipe-viewer style metadata, synced progress descriptors, and runtime timelines.

## Brain AI

The v1.3 AI API now includes typed memory keys, common memories, typed sensor builders, behavior presets, schedule builders, group AI descriptors, group memory sharing caps, and inspector snapshots. Existing `GoalDefinition` and `BrainDefinition` builders remain available.

## Structures

Structure authoring now includes stable jigsaw pool definitions, rule graphs with AND/OR/NOT composition, biome/height/noise/nearby/distance/custom rules, template capture descriptors, procedural room/corridor/tower/cave/ruin/cluster/arena/gateway helpers, simulation reports, and jigsaw graph debugging.

## Data-Driven Content

`NexusDataDefinitions` now covers simple items, blocks, block entities, entities, projectiles, food, tools, armor presets, machines, multiblocks, rituals, structures, biome modifiers, dimensions, automation networks, progression entries, and guide pages. Schema descriptors include version metadata, field descriptions, enum lists, examples, and deprecation maps. Cross-file validation reports missing references, unsafe definitions, unused definitions, and suggestions.

## Authoring

`NexusAuthoring` provides developer-only descriptors for visual definition, multiblock, ritual, automation, structure, and balance editors. It includes session gating, read-only mode, approved export roots, overwrite confirmation, export logging, in-world multiblock capture descriptors, ritual drafts, automation graph views, and structure drafts.

## Progression

`NexusProgression` provides unlock graphs without becoming a full quest engine. Nodes support dependencies, hidden/optional/repeatable flags, player/team/global scope, client sync metadata, advancement/item/block/entity/ritual/multiblock/dimension/structure/machine/custom conditions, recipe/advancement/guide/ritual/machine/message actions, guide-page generation, cycle detection, and runtime inspection.

## Simulation and Dashboards

`NexusSimulation` adds worldgen, economy, combat, diff, and dashboard descriptors. Gradle report tasks cover multiblock, ritual, brain AI, progression, worldgen simulation, recipe simulation, schema generation, guide drafts, and balance dashboard output.

## Commands

The `/nexus` tree includes multiblock validation/export/preview, ritual list/validate/start/cancel placeholders for context-driven runtime use, brain dumps, structure simulation, progression inspection, balance dashboard output, and compatibility reports.

## Compatibility and Safety

Compatibility descriptors cover protection checks, map markers, guidebook pages, recipe transfer, accessory slots, permissions, and dashboards. `NexusSafety` includes multiblock, ritual, data-driven, visual-authoring, automation, AI, and teleport safety policies.
