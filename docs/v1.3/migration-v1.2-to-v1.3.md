# v1.2 to v1.3 Migration

v1.3 keeps the stable v1.2 machine, transfer, energy, fluid, worldgen, entity, resource, recipe viewer, and diagnostics APIs available. The removed embedded example mod is replaced by source compatibility checks over the public v1.2 entry points.

## Machine and Recipe Data

Old preview machine JSON can be upgraded with `NexusV13Migrations.migrateMachinePreview`. It renames `energyCost` to `energy`, `processTicks` to `ticks`, and adds `schema_version`.

Machine recipes can now be grouped with `NexusRecipeFamilies.family`. Use recipe families when a machine has item inputs, fluid inputs, chance outputs, guide chapters, or viewer-facing docs.

## Transfer and Automation

Existing side-aware inventory, energy, and fluid transfer APIs remain the low-level transport surface. Use `NexusAutomation.network` when you need routing, filter planning, loop detection, or throughput simulation across multiple machines.

## Worldgen and Entity Helpers

Old worldgen helper JSON can be migrated with `NexusV13Migrations.migrateWorldgenHelper`. Entity goal descriptor data can be migrated with `NexusV13Migrations.migrateEntityGoal`, which renames `flags` to `controls`.

## Runtime Content

If your mod extends `NexusMod`, no extra loader code is needed for v1.3 generated runtime content. The bootstrap sequence installs automation blocks, portal blocks, generated entity types, dynamic-registry JSON, assets, translations, and loot/data files before registry registration and resource loading. Mods with custom bootstraps should call `NexusRuntimeContent.install(modId)` before `NexusRegistries.registerAll(modId)`.

## Config and Balance Profiles

Use `NexusV13Migrations.migrateBalanceProfile` to add a profile version to older balancing JSON. Balance values should move into `NexusBalance.BalanceReport` metrics so CI can diff them.

## Debug Workbench

Add v1.3 tabs through `NexusDebugWorkbench.register`. Command coverage now includes `/nexus dump dimensions`, `/nexus dump structures`, `/nexus dump ai`, `/nexus dump automation`, `/nexus validate data-definitions`, and `/nexus simulate balance`.
