# Client Runtime In v1.2

v1.2 separates common-safe client descriptors from actual client runtime registration. Common code declares intent with IDs and metadata. Client code installs keybinds, HUD layers, render layers, color providers, renderers, particles, sounds, debug screens, config screens, and machine screens.

## Common-Safe Descriptors

```java
NexusClientDescriptors.register(new ClientDescriptor.Keybind("ruby_debug",
        "key.example.ruby_debug", "key.categories.example", 82));

NexusClientDescriptors.register(new ClientDescriptor.DebugLayer("machine_state", "Machine State", true));
NexusClientDescriptors.register(new ClientDescriptor.RenderLayer("ruby_ore_cutout", id("ruby_ore"), "cutout"));
NexusClientDescriptors.register(new ClientDescriptor.ColorProvider("ruby_tint", id("ruby"), 0xE43757));
NexusClientDescriptors.register(new ClientDescriptor.ParticleEffect("ruby_spark", id("ruby_spark"), 0xE43757, 0.25F));
```

These records do not import client-only Minecraft classes and can be safely created from common code.

## Runtime Registry

Use `ClientEffectRegistry` from client initialization paths for concrete runtime callbacks:

```java
ClientEffectRegistry.keybind(id("open_debug"), "key.example.open_debug",
        "key.categories.example", GLFW.GLFW_KEY_R, () -> openDebugScreen());

ClientEffectRegistry.hudLayer(id("machine_status"), (graphics, tickDelta) ->
        graphics.drawString(Minecraft.getInstance().font, "Ruby Press", 4, 4, 0xFFFFFF));
```

Renderer/color/layer helpers:

- `entityRenderer`
- `blockEntityRenderer`
- `blockRenderLayer`
- `fluidRenderLayer`
- `itemColor`
- `blockColor`

`ClientEffectRuntime.install()` is called by `NexusCoreClient`.

## Machine Screens

`NexusMachineScreen` renders `MachineScreenLayout` with owo. Supported widgets include:

- Progress arrows.
- Energy bars.
- Fluid tanks.
- Redstone mode controls.
- Side config controls.
- Item slots and inventory groups.
- Labels, text, buttons, and custom fallbacks.

Use:

```java
NexusMachineScreens.open(parent, title, layout, bindings, storage, tank, inventory, state);
```

The debug browser can preview the first registered machine through `openFirstPreview`.

## Config Screens

`NexusConfigScreens.create` and `open` render typed `NexusConfig` options using owo components. Common code can use `OwoConfigBridge` to avoid direct client imports.

## Debug And Profiler

`NexusDebugScreen` shows registered debug sections and can open machine previews. `NexusProfilerHud` renders `NamedProfiler` data when enabled by keybind.

## Example

The example mod registers common-safe descriptors in `NexusCoreExampleSystems.registerBeforeRegistries` and demonstrates screen specs, widgets, forms, HUD overlays, and machine bindings in `demonstrateUi`.
