package com.rollylindenshnizzer.nexuscore.testmod;

import com.rollylindenshnizzer.nexuscore.api.NexusMod;

public final class NexusCoreTestMod {
    public static final NexusMod MOD = NexusMod.create("nexuscore_testmod");
    private static boolean initialized;

    private NexusCoreTestMod() {
    }

    public static synchronized void init() {
        if (initialized) {
            return;
        }
        initialized = true;
        TestContent.register();
        TestEvents.register();
        TestCommands.register();
        TestNetwork.register();
        TestConfig.register();
        TestDataGen.register();
        MOD.logger().info("NexusCore Test Mod initialized.");
    }
}
