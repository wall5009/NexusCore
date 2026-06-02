package com.example.advancedexample;

public final class LoaderHooks {
    private LoaderHooks() {
    }

    public static void register() {
        AdvancedExampleMod.MOD.logger().info("Common loader hooks active.");
        invoke("com.example.advancedexample.FabricHooks");
    }

    private static void invoke(String className) {
        try {
            Class.forName(className).getMethod("register").invoke(null);
        } catch (ReflectiveOperationException ignored) {
        }
    }
}
