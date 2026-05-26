package com.rollylindenshnizzer.nexuscore.config;

public final class OwoConfigBridge {
    public static boolean isOwoPresent() {
        try {
            Class.forName("io.wispforest.owo.config.ConfigWrapper");
            return true;
        } catch (ClassNotFoundException ignored) {
            return false;
        }
    }

    public static String status() {
        return isOwoPresent() ? "owo-lib config classes are available" : "owo-lib config classes are not on the current classpath";
    }

    public static Object createGeneratedScreen(NexusConfig config, Object parentScreen) {
        if (!isOwoPresent()) {
            throw new IllegalStateException("owo-lib is required to create NexusCore generated config screens");
        }
        try {
            Class<?> screenClass = Class.forName("com.rollylindenshnizzer.nexuscore.client.config.NexusConfigScreen");
            Class<?> vanillaScreenClass = Class.forName("net.minecraft.client.gui.screens.Screen");
            return screenClass.getConstructor(NexusConfig.class, vanillaScreenClass).newInstance(config, parentScreen);
        } catch (ReflectiveOperationException exception) {
            throw new IllegalStateException("Failed to create generated NexusCore config screen", exception);
        }
    }

    private OwoConfigBridge() {
    }
}
