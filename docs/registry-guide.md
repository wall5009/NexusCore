# Registry Guide

NexusCore wraps Architectury deferred registries with a `NexusRegistryGroup` per mod ID. The group records content in `NexusContentManifest`, validates duplicate paths, exposes registry reports, and keeps registration timing consistent across Fabric and NeoForge.

## Registry Groups

Use high-level builders for normal content:

```java
NexusItems.item(MOD_ID, "ruby").register();
NexusBlocks.block(MOD_ID, "ruby_ore").register();
NexusSounds.variable(MOD_ID, "ruby_press/start");
```

Use the raw group when there is no specialized builder:

```java
NexusRegistryGroup group = NexusRegistries.group(MOD_ID);
group.particles().register("ruby_spark", () -> new SimpleParticleType(false));
```

The group owns deferred registers for:

- Items
- Blocks
- Block entities
- Menus
- Entity types
- Sound events
- Particle types
- Creative tabs
- Recipe types
- Recipe serializers
- Data component types

## Timing

Declare registry entries from `NexusMod.beforeRegistries` or from `ContentModule.register`. `NexusMod` calls `NexusRegistries.group(modId).registerAll()` after modules are initialized.

Avoid calling `RegistrySupplier.get()` for objects that depend on vanilla registry completion during declaration. Prefer passing suppliers or IDs where possible.

## Creative Tabs

```java
RegistrySupplier<CreativeModeTab> tab = NexusItems.creativeTab(MOD_ID, "main")
        .icon(() -> new ItemStack(Items.DIAMOND))
        .register();
```

Then attach content:

```java
NexusItems.item(MOD_ID, "ruby")
        .creativeTab(tab)
        .modelGenerated()
        .register();
```

## Content Modules

Content modules are useful once a mod has independent systems:

```java
public final class MachinesModule implements ContentModule {
    @Override
    public String id() {
        return "machines";
    }

    @Override
    public Collection<String> dependencies() {
        return List.of("materials");
    }

    @Override
    public void register(NexusRegistryGroup registries) {
        NexusBlocks.block("example", "crusher").register();
    }

    @Override
    public void dataGeneration() {
        NexusData.plan("example").translation("block.example.crusher", "Crusher");
    }
}
```

Return modules from `NexusMod.modules()`. `ContentModuleManager` sorts dependencies, rejects duplicates, detects cycles, and calls `register`, `dataGeneration`, and `compatibility`.

The scaffolding and GameTest coverage uses two content modules in `NexusCoreGameTestScenarios`.

## Manifest And Reports

Every registry helper records to `NexusContentManifest`. Datagen also records generated files. The resulting `data/<modid>/nexus.content.json` is useful for:

- Docs generation
- Debug browser summaries
- Release review
- Missing-content investigations

Generate a registry report:

```java
NexusDiagnostics.Report report = NexusRegistryReports.report(NexusRegistries.group(MOD_ID));
```

## Custom Registry Specs

`CustomRegistrySpec<T>` documents a custom registry's ID, type, sync policy, and persistence policy:

```java
CustomRegistrySpec<MyTrait> traits = CustomRegistrySpec
        .builder(NexusIds.id(MOD_ID, "traits"), MyTrait.class)
        .synced()
        .build();
```

Pair custom specs with `TypedDataLoader` and `DataDrivenRegistry` when the registry is datapack-backed.

## Release Checks

- Duplicate IDs should fail during registration.
- Missing lookups should use `NexusRegistries.require` so errors include a reason.
- Every generated content family should appear in `nexus.content.json`.
- Large mods should keep content modules acyclic and named by subsystem.
