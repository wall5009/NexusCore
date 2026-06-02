package com.example.advancedexample;

import com.rollylindenshnizzer.nexuscore.api.NexusMod;
import com.rollylindenshnizzer.nexuscore.api.item.NexusItems;
import com.rollylindenshnizzer.nexuscore.api.platform.NexusPlatform;

public final class AdvancedExampleMod {
    public static final NexusMod MOD = NexusMod.create("advancedexample");

    private AdvancedExampleMod() {
    }

    public static void init() {
        MOD.logger().info("Running on " + NexusPlatform.target().targetId());
        NexusItems.create(MOD, "shared_item").register();
        VersionHooks.register();
        LoaderHooks.register();
    }
}
