# Getting Started

NexusCore is a Minecraft 1.21.1 Architectury library mod for Fabric and NeoForge. It provides common registration builders, datagen, config, diagnostics, networking helpers, machines, transfer abstractions, recipe viewer integration, client descriptors, testing helpers, and build tooling.

For a complete package-by-package map, read `docs/system-catalog.md`. For generated examples, read `docs/scaffolding-walkthrough.md` and `docs/v1.3/cookbook.md`.

## Project Shape

A typical NexusCore consumer has three source sets:

```text
my-mod/
  common/   shared content, config, datagen plans, machines, packets, descriptors
  fabric/   Fabric entrypoint, Fabric-only bridges, Fabric datagen
  neoforge/ NeoForge @Mod entrypoint, NeoForge-only bridges, NeoForge datagen
```

The common module should own most mod logic. Loader modules should stay thin unless a loader API is genuinely different.

## Dependencies

Use NexusCore as a mod dependency on both loaders and keep Architectury, owo-lib, Minecraft, and Java versions aligned with the version catalog used by NexusCore.

Optional integrations are intentionally optional:

- JEI, EMI, and REI are optional recipe viewer integrations.
- Team Reborn Energy is the Fabric energy bridge.
- Fabric Transfer is the Fabric item/fluid transfer bridge.
- NeoForge capabilities are used for NeoForge energy/fluid/item exposure.
- owo-lib is required by NexusCore's generated client screens.

## Bootstrap

The smallest common bootstrap uses `NexusMod`:

```java
public final class ExampleContent extends NexusMod {
    public static final String MOD_ID = "example";
    private static final ExampleContent INSTANCE = new ExampleContent();

    private ExampleContent() {
        super(MOD_ID);
    }

    public static void bootstrap() {
        INSTANCE.init();
    }

    @Override
    protected void beforeRegistries() {
        // Registry declarations go here.
    }

    @Override
    protected void onInitialize() {
        // Runtime setup, validation, commands, packets, and debug sections go here.
    }
}
```

Fabric entrypoint:

```java
public final class ExampleFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        NexusCore.init();
        ExampleContent.bootstrap();
    }
}
```

NeoForge entrypoint:

```java
@Mod(ExampleContent.MOD_ID)
public final class ExampleNeoForge {
    public ExampleNeoForge(IEventBus modBus) {
        NexusCore.init();
        ExampleContent.bootstrap();
    }
}
```

## Registration Pattern

Declare content before the registry group is registered:

```java
RegistrySupplier<CreativeModeTab> tab = NexusItems.creativeTab(MOD_ID, "main")
        .icon(() -> new ItemStack(Items.DIAMOND))
        .register();

RegistrySupplier<NexusItem> ruby = NexusItems.item(MOD_ID, "ruby")
        .creativeTab(tab)
        .tooltip("tooltip.example.ruby")
        .modelGenerated()
        .register();

RegistrySupplier<Block> rubyOre = NexusBlocks.block(MOD_ID, "ruby_ore")
        .strength(3.0F, 3.0F)
        .requiresCorrectTool()
        .withBlockItem()
        .simpleCubeModel()
        .dropsSelf()
        .mineableWithPickaxe()
        .needsIronTool()
        .register();
```

`NexusMod` calls `NexusRegistries.group(modId).registerAll()` after `beforeRegistries` and content module initialization.

## Datagen

Create one common plan and consume it from both loaders:

```java
public static NexusData.DataPlan populateGeneratedData() {
    return NexusData.plan(MOD_ID)
            .translation("item.example.ruby", "Ruby")
            .data("recipe/ruby_from_smelting.json",
                    RecipeJsonBuilder.cooking("minecraft:smelting", "misc",
                            "example:raw_ruby", "example:ruby", 0.7F, 200).build());
}
```

Fabric uses `FabricDataGenerator.Pack.addProvider`. NeoForge uses `GatherDataEvent.addProvider`. The scaffolding guide shows how to generate focused starter classes.

Run:

```powershell
.\gradlew.bat :fabric:runDatagen
.\gradlew.bat :neoforge:runDatagen
```

## First Systems To Add

Most mods start with this sequence:

1. Register a creative tab, items, and blocks.
2. Add translations, models, blockstates, tags, loot, and recipes through `NexusData`.
3. Define `NexusConfig` options and call `validateAll`.
4. Install a debug command and at least one `DebugRegistry.section`.
5. Add a `ValidationSuite` for content invariants.
6. Add recipe viewer categories/displays if the content introduces custom processing.
7. Add machines, transfer, worldgen, entities, or client descriptors as needed.

## Validation Commands

From the repository root:

```powershell
.\gradlew.bat build
.\gradlew.bat runNexusValidation
.\gradlew.bat :fabric:runGametest
.\gradlew.bat :neoforge:runGametest
```

For release compatibility:

```powershell
.\gradlew.bat checkBinaryCompatibility -PnexusApiBaseline=common/build/libs/nexuscore-common-1.0.0-build.3.jar -PnexusRelease=true
```

## Where To Go Next

- `docs/system-catalog.md`: every package and subsystem.
- `docs/scaffolding-walkthrough.md`: generated example classes and workflow.
- `docs/v1.3/index.md`: the v1.3 systems overview.
- `docs/registry-guide.md`: registries and content modules.
- `docs/item-block-guide.md`: content builders.
- `docs/config-guide.md`: typed config and generated screens.
- `docs/datagen-guide.md`: generated resources and validation.
- `docs/machine-guide.md`: machines, inventory, energy, fluids, and UI bindings.
- `docs/compatibility-guide.md`: JEI, EMI, REI, owo-lib, Fabric, and NeoForge compatibility.
- `docs/testing-guide.md`: validation suites, GameTests, ABI checks, and CI gates.
