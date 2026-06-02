package com.example.advancedexample;

public final class FabricHooks {
    private FabricHooks() {
    }

    public static void register() {
        AdvancedExampleMod.MOD.logger().info("Fabric-family hooks active.");
    }
}
