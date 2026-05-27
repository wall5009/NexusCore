package com.rollylindenshnizzer.nexuscore.client;

import com.rollylindenshnizzer.nexuscore.api.NexusStable;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;

@NexusStable(since = "1.2")
public record ClientEffectSpec(ResourceLocation id,
                               Type type,
                               Map<String, String> properties,
                               boolean debugVisible) {
    public ClientEffectSpec {
        properties = properties == null ? Map.of() : Map.copyOf(properties);
    }

    public enum Type {
        PARTICLE,
        SOUND,
        BLOCK_ENTITY_RENDERER,
        ENTITY_RENDERER,
        HUD_LAYER,
        KEYBIND
    }
}
