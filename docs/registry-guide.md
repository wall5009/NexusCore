# Registry Guide

NexusCore wraps Architectury registries with small builders and a registry group per mod ID.

## Registry Groups

Use `NexusRegistries.group(modId)` when you need direct access to the group. Most users should prefer the item, block, entity, sound, menu, and block entity builders, which register with the right group automatically.

Duplicate IDs are rejected through `DuplicateRegistrationException`, and missing lookups use `MissingRegistryEntryException` for clearer failures.

## Creative Tabs

```java
RegistrySupplier<CreativeModeTab> tab = NexusItems.creativeTab(MOD_ID, "main")
        .icon(() -> new ItemStack(Items.DIAMOND))
        .register();
```

Pass the tab supplier into item and block builders:

```java
NexusItems.item(MOD_ID, "ruby")
        .creativeTab(tab)
        .register();
```

## Content Modules

For larger mods, group registration into modules:

```java
public final class MachinesModule implements ContentModule {
    @Override
    public String id() {
        return "machines";
    }

    @Override
    public void register() {
        NexusBlocks.block("example", "crusher").register();
    }
}
```

Return modules from `NexusMod.modules()`. `ContentModuleManager` sorts dependencies and initializes each module once.

## Conditions

`NexusConditions` stores reusable boolean gates for optional content. Use this when a feature depends on another mod, a config option, or an environment check.
