package com.example.advancedexample;

public final class VersionHooks {
    private VersionHooks() {
    }

    public static void register() {
        AdvancedExampleMod.MOD.logger().info("Common version hooks active.");
        invoke("com.example.advancedexample.Mc1211Hooks");
    }

    private static void invoke(String className) {
        try {
            Class.forName(className).getMethod("register").invoke(null);
        } catch (ReflectiveOperationException ignored) {
        }
    }
}
