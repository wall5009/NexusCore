package com.rollylindenshnizzer.nexuscore.testmod;

import com.rollylindenshnizzer.nexuscore.api.config.NexusConfig;

public final class TestConfig {
    public static NexusConfig CONFIG;

    private TestConfig() {
    }

    public static void register() {
        CONFIG = NexusConfig.create("nexuscore_testmod")
            .booleanValue("enableRuby", true)
            .intValue("rubySpawnRate", 8, 0, 64)
            .stringValue("welcomeMessage", "Welcome from NexusCore")
            .build();
        CONFIG.save();
    }
}
