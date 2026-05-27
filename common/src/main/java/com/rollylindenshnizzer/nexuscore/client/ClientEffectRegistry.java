package com.rollylindenshnizzer.nexuscore.client;

import com.rollylindenshnizzer.nexuscore.api.NexusStable;
import com.rollylindenshnizzer.nexuscore.debug.DebugRegistry;
import dev.architectury.registry.client.level.entity.EntityRendererRegistry;
import dev.architectury.registry.client.rendering.BlockEntityRendererRegistry;
import dev.architectury.registry.client.rendering.ColorHandlerRegistry;
import dev.architectury.registry.client.rendering.RenderTypeRegistry;
import net.minecraft.client.color.block.BlockColor;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.material.Fluid;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

@NexusStable(since = "1.2")
public final class ClientEffectRegistry {
    private static final Map<ResourceLocation, ClientEffectSpec> SPECS = new LinkedHashMap<>();
    private static final Map<ResourceLocation, Runnable> KEYBIND_ACTIONS = new LinkedHashMap<>();
    private static final Map<ResourceLocation, HudLayer> HUD_LAYERS = new LinkedHashMap<>();
    private static final List<Runnable> RENDER_REGISTRATIONS = new ArrayList<>();

    static {
        DebugRegistry.section("nexuscore.client_effects", () -> Integer.toString(SPECS.size()));
        DebugRegistry.section("nexuscore.client_runtime_renderers", () -> Integer.toString(RENDER_REGISTRATIONS.size()));
    }

    public static ClientEffectSpec register(ClientEffectSpec spec) {
        SPECS.put(spec.id(), spec);
        return spec;
    }

    public static ClientEffectSpec keybind(ResourceLocation id, String translationKey, String category, int defaultKey, Runnable action) {
        register(new ClientEffectSpec(id, ClientEffectSpec.Type.KEYBIND, Map.of(
                "translationKey", translationKey,
                "category", category,
                "defaultKey", Integer.toString(defaultKey)), true));
        KEYBIND_ACTIONS.put(id, action == null ? () -> { } : action);
        return SPECS.get(id);
    }

    public static ClientEffectSpec hudLayer(ResourceLocation id, HudLayer layer) {
        register(new ClientEffectSpec(id, ClientEffectSpec.Type.HUD_LAYER, Map.of("label", id.toString()), true));
        HUD_LAYERS.put(id, layer);
        return SPECS.get(id);
    }

    public static <T extends Entity> void entityRenderer(Supplier<? extends EntityType<? extends T>> type,
                                                         EntityRendererProvider<T> provider) {
        RENDER_REGISTRATIONS.add(() -> EntityRendererRegistry.register(type, provider));
    }

    public static <T extends BlockEntity> void blockEntityRenderer(BlockEntityType<T> type,
                                                                   BlockEntityRendererProvider<? super T> provider) {
        RENDER_REGISTRATIONS.add(() -> BlockEntityRendererRegistry.register(type, provider));
    }

    public static void blockRenderLayer(RenderType type, Supplier<? extends Block> block) {
        RENDER_REGISTRATIONS.add(() -> RenderTypeRegistry.register(type, block.get()));
    }

    public static void fluidRenderLayer(RenderType type, Supplier<? extends Fluid> fluid) {
        RENDER_REGISTRATIONS.add(() -> RenderTypeRegistry.register(type, fluid.get()));
    }

    @SafeVarargs
    public static void itemColor(ItemColor color, Supplier<? extends Item>... items) {
        RENDER_REGISTRATIONS.add(() -> ColorHandlerRegistry.registerItemColors(color, items));
    }

    @SafeVarargs
    public static void blockColor(BlockColor color, Supplier<? extends Block>... blocks) {
        RENDER_REGISTRATIONS.add(() -> ColorHandlerRegistry.registerBlockColors(color, blocks));
    }

    public static Collection<ClientEffectSpec> specs() {
        return java.util.List.copyOf(SPECS.values());
    }

    static Map<ResourceLocation, Runnable> keybindActions() {
        return Map.copyOf(KEYBIND_ACTIONS);
    }

    static Map<ResourceLocation, HudLayer> hudLayers() {
        return Map.copyOf(HUD_LAYERS);
    }

    static void installRenderRegistrations(Consumer<RuntimeException> errorHandler) {
        for (Runnable registration : List.copyOf(RENDER_REGISTRATIONS)) {
            try {
                registration.run();
            } catch (RuntimeException exception) {
                errorHandler.accept(exception);
            }
        }
    }

    @FunctionalInterface
    public interface HudLayer {
        void render(GuiGraphics graphics, float tickDelta);
    }

    private ClientEffectRegistry() {
    }
}
