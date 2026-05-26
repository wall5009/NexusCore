package com.rollylindenshnizzer.nexuscore.client.config;

import com.rollylindenshnizzer.nexuscore.config.NexusConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

public final class NexusConfigScreens {
    public static Screen create(NexusConfig config, Screen parent) {
        return new NexusConfigScreen(config, parent);
    }

    public static void open(NexusConfig config, Screen parent) {
        Minecraft.getInstance().setScreen(create(config, parent));
    }

    private NexusConfigScreens() {
    }
}
