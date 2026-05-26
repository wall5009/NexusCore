package com.rollylindenshnizzer.nexuscore.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.rollylindenshnizzer.nexuscore.client.debug.NexusDebugScreen;
import com.rollylindenshnizzer.nexuscore.core.NexusLifecycle;
import com.rollylindenshnizzer.nexuscore.debug.DebugRegistry;
import dev.architectury.event.events.client.ClientLifecycleEvent;
import dev.architectury.event.events.client.ClientTickEvent;
import dev.architectury.registry.client.keymappings.KeyMappingRegistry;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;

public final class NexusCoreClient {
    private static boolean initialized;
    private static KeyMapping debugScreenKey;

    public static void init() {
        if (initialized) {
            return;
        }
        initialized = true;

        debugScreenKey = new KeyMapping("key.nexuscore.debug_screen", InputConstants.KEY_F9, "key.categories.nexuscore");
        KeyMappingRegistry.register(debugScreenKey);

        ClientLifecycleEvent.CLIENT_SETUP.register(client -> NexusLifecycle.fire(NexusLifecycle.Phase.CLIENT_INIT));
        ClientTickEvent.CLIENT_POST.register(client -> {
            while (debugScreenKey.consumeClick()) {
                Minecraft.getInstance().setScreen(new NexusDebugScreen());
            }
        });

        DebugRegistry.section("client_descriptors", () -> Integer.toString(NexusClientDescriptors.descriptors().size()));
    }

    private NexusCoreClient() {
    }
}
