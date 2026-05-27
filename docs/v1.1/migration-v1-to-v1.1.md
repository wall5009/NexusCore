# Migrating from v1.0 to v1.1

v1.1 keeps v1 source compatibility. Existing item/block/config/network/recipe-viewer code should compile.

Recommended upgrades:

- Add `@NexusStable` or `@NexusExperimental` to public extension APIs.
- Use `RecipeViewerDisplay.builder(...)` for custom recipe viewer layouts.
- Use `ConfigSchemaExporter` to publish config docs.
- Use `NexusNetworking.channel(...).protocolVersion("1.1")` for readable protocol diagnostics.
- Use `NexusComponents` for new item data instead of raw NBT.
- Run `runNexusValidation` in CI.

Removed config keys can be reported with `NexusMigrations.forMod(modid).removedConfigKey(...)`.
