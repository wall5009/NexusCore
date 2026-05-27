package com.rollylindenshnizzer.nexuscore.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.rollylindenshnizzer.nexuscore.NexusCore;
import dev.architectury.event.events.client.ClientGuiEvent;
import dev.architectury.event.events.client.ClientTickEvent;
import dev.architectury.registry.client.keymappings.KeyMappingRegistry;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.core.particles.SimpleParticleType;

import java.util.LinkedHashMap;
import java.util.Map;

public final class ClientEffectRuntime {
    private static final Map<ResourceLocation, KeyMapping> KEY_MAPPINGS = new LinkedHashMap<>();
    private static boolean installed;

    public static void install() {
        if (installed) {
            return;
        }
        installed = true;

        ClientEffectRegistry.installRenderRegistrations(exception ->
                NexusCore.LOGGER.warn("Failed to install NexusCore client renderer registration", exception));
        installKeyMappings();

        ClientTickEvent.CLIENT_POST.register(client -> {
            for (Map.Entry<ResourceLocation, KeyMapping> entry : KEY_MAPPINGS.entrySet()) {
                while (entry.getValue().consumeClick()) {
                    trigger(entry.getKey());
                }
            }
        });
        ClientGuiEvent.RENDER_HUD.register((graphics, deltaTracker) -> {
            float delta = 0.0F;
            ClientEffectRegistry.hudLayers().forEach((id, layer) -> layer.render(graphics, delta));
            for (ClientEffectSpec spec : ClientEffectRegistry.specs()) {
                if (spec.type() == ClientEffectSpec.Type.HUD_LAYER && !ClientEffectRegistry.hudLayers().containsKey(spec.id())) {
                    graphics.drawString(Minecraft.getInstance().font,
                            spec.properties().getOrDefault("label", spec.id().toString()), 6, 6 + KEY_MAPPINGS.size() * 10,
                            parseColor(spec.properties().get("color"), 0xFFFFFF), true);
                }
            }
        });
    }

    public static void trigger(ResourceLocation id) {
        Runnable action = ClientEffectRegistry.keybindActions().get(id);
        if (action != null) {
            action.run();
        }
        ClientEffectSpec spec = ClientEffectRegistry.specs().stream()
                .filter(candidate -> candidate.id().equals(id))
                .findFirst()
                .orElse(null);
        if (spec == null) {
            return;
        }
        switch (spec.type()) {
            case PARTICLE -> spawnParticle(spec);
            case SOUND -> playSound(spec);
            default -> {
            }
        }
    }

    private static void installKeyMappings() {
        for (ClientDescriptor descriptor : NexusClientDescriptors.descriptors()) {
            if (descriptor instanceof ClientDescriptor.Keybind keybind) {
                registerKey(keybind.id(), keybind.translationKey(), keybind.category(), keybind.defaultKey());
            }
        }
        for (ClientEffectSpec spec : ClientEffectRegistry.specs()) {
            if (spec.type() == ClientEffectSpec.Type.KEYBIND) {
                registerKey(spec.id().toString(),
                        spec.properties().getOrDefault("translationKey", "key." + spec.id().getNamespace() + "." + spec.id().getPath()),
                        spec.properties().getOrDefault("category", "key.categories.nexuscore"),
                        parseInt(spec.properties().get("defaultKey"), InputConstants.UNKNOWN.getValue()));
            }
        }
    }

    private static void registerKey(String id, String translationKey, String category, int defaultKey) {
        ResourceLocation key = ResourceLocation.tryParse(id.contains(":") ? id : "nexuscore:" + id.replace('.', '_'));
        if (key == null || KEY_MAPPINGS.containsKey(key)) {
            return;
        }
        KeyMapping mapping = new KeyMapping(translationKey, defaultKey, category);
        KEY_MAPPINGS.put(key, mapping);
        KeyMappingRegistry.register(mapping);
    }

    private static void spawnParticle(ClientEffectSpec spec) {
        Minecraft client = Minecraft.getInstance();
        Player player = client.player;
        if (player == null || client.level == null) {
            return;
        }
        ResourceLocation particleId = ResourceLocation.tryParse(spec.properties().getOrDefault("particle", spec.id().toString()));
        if (particleId == null) {
            return;
        }
        var particleType = BuiltInRegistries.PARTICLE_TYPE.get(particleId);
        if (particleType instanceof SimpleParticleType simple) {
            int count = parseInt(spec.properties().get("count"), 8);
            for (int i = 0; i < count; i++) {
                client.level.addParticle(simple, player.getX(), player.getY() + 1.0, player.getZ(),
                        (client.level.random.nextDouble() - 0.5) * 0.08,
                        client.level.random.nextDouble() * 0.05,
                        (client.level.random.nextDouble() - 0.5) * 0.08);
            }
        }
    }

    private static void playSound(ClientEffectSpec spec) {
        Minecraft client = Minecraft.getInstance();
        ResourceLocation soundId = ResourceLocation.tryParse(spec.properties().getOrDefault("sound", spec.id().toString()));
        if (soundId == null) {
            return;
        }
        SoundEvent sound = SoundEvent.createVariableRangeEvent(soundId);
        float pitch = parseFloat(spec.properties().get("pitch"), 1.0F);
        client.getSoundManager().play(SimpleSoundInstance.forUI(sound, pitch));
        if (client.player != null && client.level != null && Boolean.parseBoolean(spec.properties().getOrDefault("broadcast", "false"))) {
            client.level.playLocalSound(client.player.getX(), client.player.getY(), client.player.getZ(), sound,
                    SoundSource.PLAYERS, parseFloat(spec.properties().get("volume"), 1.0F), pitch, false);
        }
    }

    private static int parseInt(String value, int fallback) {
        try {
            return value == null ? fallback : Integer.parseInt(value);
        } catch (NumberFormatException exception) {
            return fallback;
        }
    }

    private static float parseFloat(String value, float fallback) {
        try {
            return value == null ? fallback : Float.parseFloat(value);
        } catch (NumberFormatException exception) {
            return fallback;
        }
    }

    private static int parseColor(String value, int fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        try {
            return Integer.decode(value);
        } catch (NumberFormatException exception) {
            return fallback;
        }
    }

    private ClientEffectRuntime() {
    }
}
