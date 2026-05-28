# Gradle Scaffolding and Validation

The NexusCore Gradle plugin now includes v1.3 scaffolds:

- `nexusCreateDimension`
- `nexusCreatePortal`
- `nexusCreateStructure`
- `nexusCreateBiome`
- `nexusCreateAiGoal`
- `nexusCreateAutomationNetwork`
- `nexusCreateDataDefinition`
- `nexusCreateBalanceReport`

Existing v1.2 scaffolds for machines, recipes, energy, fluids, worldgen, entities, projectiles, datapack loaders, configs, packets, screens, GameTests, and compatibility modules remain.

Validation tasks:

- `runNexusDimensionValidation`
- `runNexusStructureValidation`
- `runNexusAIValidation`
- `runNexusAutomationValidation`
- `runNexusDefinitionValidation`
- `runNexusBalanceDiff`
- `checkV12SourceCompatibility`
- `runNexusValidation`

`checkV12SourceCompatibility` replaces the old embedded example compilation check. It verifies that the v1.2 public entry points the scaffolded coverage used still exist after the scaffolding and GameTest coverageule removal.

