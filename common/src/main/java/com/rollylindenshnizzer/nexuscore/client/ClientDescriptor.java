package com.rollylindenshnizzer.nexuscore.client;

import net.minecraft.resources.ResourceLocation;

public sealed interface ClientDescriptor permits ClientDescriptor.RenderLayer, ClientDescriptor.ColorProvider,
        ClientDescriptor.Keybind, ClientDescriptor.ParticleEffect, ClientDescriptor.DebugLayer {
    String id();

    record RenderLayer(String id, ResourceLocation target, String layer) implements ClientDescriptor {
    }

    record ColorProvider(String id, ResourceLocation target, int defaultColor) implements ClientDescriptor {
    }

    record Keybind(String id, String translationKey, String category, int defaultKey) implements ClientDescriptor {
    }

    record ParticleEffect(String id, ResourceLocation particle, int color, float density) implements ClientDescriptor {
    }

    record DebugLayer(String id, String label, boolean enabledByDefault) implements ClientDescriptor {
    }
}
