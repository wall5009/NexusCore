package com.rollylindenshnizzer.nexuscore.testmod;

import com.rollylindenshnizzer.nexuscore.api.command.NexusCommands;

public final class TestCommands {
    private TestCommands() {
    }

    public static void register() {
        NexusCommands.literal("nexuscore_test")
            .executes(ctx -> {
                ctx.reply("Hello from NexusCore!");
                return 1;
            })
            .register();
    }
}
