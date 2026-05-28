# Balance, Testing, Diagnostics, Performance, and Security

## Balance

`NexusBalance.report` stores named metrics such as energy per tick, items per minute, fluid per operation, spawn weight, route throughput, or structure frequency. `NexusBalance.diff` compares reports and produces CI-friendly change summaries.

`SimulationScenario` provides deterministic helper simulations for tuning. It is not a Minecraft tick engine replacement; it is designed for library-level checks, docs, and CI.

## Testing

The shared `NexusCoreGameTestScenarios` now includes v1.3 scenarios for dimensions, portals, structures, biomes, AI, automation, data definitions, balance, live reload safety, recipes, and compatibility. Fabric and NeoForge wrappers register those same scenarios against the bundled bootstrap structure.

## Diagnostics

New command groups:

- `/nexus dump dimensions`
- `/nexus dump structures`
- `/nexus dump biomes`
- `/nexus dump ai`
- `/nexus dump automation`
- `/nexus dump data-definitions`
- `/nexus validate dimensions`
- `/nexus validate structures`
- `/nexus simulate automation`
- `/nexus simulate balance`

## Performance

`NexusPerformanceTools` adds bounded graph caches, cache diagnostics, performance snapshots, diffs, and budgets. Use budgets in tests or CI to catch regressions in route planning, structure scanning, biome selector evaluation, and definition reload.

## Security

`NexusSafety` centralizes automation limits, teleport bounds, data definition safety, and AI safety. It gives servers and mod authors a common policy surface before runtime systems perform expensive or risky work.

