package com.rollylindenshnizzer.nexuscore.command;

import com.rollylindenshnizzer.nexuscore.debug.DebugRegistry;
import net.minecraft.network.chat.Component;

import java.util.Map;

public final class NexusDebugCommands {
    public static void install(String rootName) {
        NexusCommands.literal(rootName)
                .permission(2)
                .executes(context -> {
                    for (Map.Entry<String, String> entry : DebugRegistry.snapshot().entrySet()) {
                        context.getSource().sendSystemMessage(Component.literal(entry.getKey() + ": " + entry.getValue()));
                    }
                    return 1;
                })
                .register();
    }

    private NexusDebugCommands() {
    }
}
