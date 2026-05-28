# Architecture

NexusCore is organized around one rule: common code declares portable intent, loader modules perform loader-specific wiring, and client-only runtime code stays behind client entrypoints.

## Layers

| Layer | Packages | Responsibility |
| --- | --- | --- |
| Bootstrap | `core` | Initialization, lifecycle, environment checks, diagnostics, task queues, IDs |
| Registration | `registry`, `item`, `block`, `blockentity`, `entity`, `sound`, `menu` | Deferred registration and content builders |
| Data | `data`, `resource`, `worldgen`, `advancement`, `loot`, `tag` | Generated JSON, typed datapack loading, validation, manifests |
| Runtime | `machine`, `inventory`, `energy`, `fluid`, `network`, `persistence`, `player`, `world`, `security` | Gameplay systems and cross-loader abstractions |
| Client | `client`, `ui` | Common-safe descriptors plus client-only owo screens, debug browser, profiler HUD, machine screen runtime |
| Integration | `compat` | Optional JEI/EMI/REI, loader transfer/capability, safe classloading |
| Tooling | `test`, `performance`, `nexus-gradle` | GameTests, assertions, benchmarks, ABI checks, docs/release tasks, scaffolding |

## Bootstrap Contract

`NexusMod` gives consumers a predictable sequence:

1. Install shared lifecycle hooks.
2. Fire `PRE_INIT`.
3. Run `beforeRegistries`.
4. Initialize dependency-sorted `ContentModule`s.
5. Register the mod's `NexusRegistryGroup`.
6. Run `onInitialize`.
7. Fire common init and validation phases.
8. Emit startup diagnostics.

Registry declarations belong before step 5. Runtime logic belongs after step 5.

## Common/Loader Boundary

Common code may:

- Define item/block/entity/machine/worldgen/resource descriptors.
- Register deferred registry entries through Architectury-backed wrappers.
- Build datagen plans.
- Declare versioned network channels.
- Define recipe viewer categories/displays.
- Create client descriptors that contain only IDs and metadata.
- Run validation and pure Java tests.

Loader code should:

- Call the common bootstrap.
- Register loader datagen providers.
- Expose Fabric Transfer, Team Reborn Energy, or NeoForge capabilities.
- Install loader-specific JEI/EMI/REI plugin classes.
- Register GameTest entrypoints.

Client code should:

- Install keybind actions, HUD layers, renderers, color providers, render layers, and screens.
- Open generated owo config screens and machine screens.
- Render debug browser/profiler UI.

## Descriptor Pattern

Most v1.2 systems use descriptors:

- `NexusMachineDefinition`
- `MachineRecipeDefinition`
- `MachineScreenLayout`
- `NexusEntityDefinition`
- `ProjectileDefinition`
- `OreGenerationBuilder`
- `ClientDescriptor`
- `ClientEffectSpec`
- `RecipeViewerDisplay`
- `PlayerAttachmentSpec`
- `CustomRegistrySpec`

Descriptors are small immutable records or builders. They are easy to validate, emit into docs/debug views, use for datagen, and adapt per loader.

## Diagnostics Pattern

Every major system exposes one or more of:

- A debug registry section.
- A validation report.
- A doctor report entry.
- A generated manifest entry.
- A GameTest or validation suite scenario.

This is deliberate. NexusCore is meant to make modding easier by making failure states visible before a user reports them.

## Optional Dependency Pattern

Optional integrations are loaded through metadata and safe classloading:

- Recipe viewers are optional. The shared API is always available; concrete JEI/EMI/REI plugins load only when present.
- owo-lib is required by NexusCore client screens but common config definitions remain server-safe.
- Fabric energy uses Team Reborn Energy. NeoForge energy uses capabilities.
- Loader bridges are isolated in loader modules so common code stays portable.

## Maintained Usage References

The embedded example mod was removed. Current usage coverage is maintained through:

- `docs/scaffolding-walkthrough.md` for generated starter classes.
- `docs/v1.3/cookbook.md` for copyable system snippets.
- `NexusCoreGameTestScenarios` for compile-checked common API coverage.
- Fabric and NeoForge GameTest wrappers for loader registration coverage.
