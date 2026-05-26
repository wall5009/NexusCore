package com.rollylindenshnizzer.nexuscore.core;

import dev.architectury.platform.Platform;
import dev.architectury.utils.Env;
import net.fabricmc.api.EnvType;

import java.util.Locale;

public final class NexusEnvironment {
    public static boolean isClient() {
        return Platform.getEnvironment() == Env.CLIENT;
    }

    public static boolean isDedicatedServer() {
        return Platform.getEnvironment() == Env.SERVER;
    }

    public static Env physicalSide() {
        return Platform.getEnvironment();
    }

    public static EnvType fabricEnvType() {
        return Platform.getEnv();
    }

    public static boolean isDevelopment() {
        return Platform.isDevelopmentEnvironment();
    }

    public static boolean isDataGeneration() {
        return boolProperty("fabric-api.datagen")
                || boolProperty("architectury.datagen")
                || boolProperty("neoforge.datagen")
                || boolEnv("DATAGEN")
                || boolEnv("NEXUSCORE_DATAGEN");
    }

    public static boolean isTestEnvironment() {
        return boolProperty("nexuscore.test")
                || boolEnv("NEXUSCORE_TEST")
                || Thread.currentThread().getName().toLowerCase(Locale.ROOT).contains("gametest");
    }

    public static boolean isModLoaded(String modId) {
        return Platform.isModLoaded(modId);
    }

    public static void runOnClient(Runnable runnable) {
        if (isClient()) {
            runnable.run();
        }
    }

    public static void runOnServer(Runnable runnable) {
        if (isDedicatedServer()) {
            runnable.run();
        }
    }

    public static void expectClient(String action) {
        if (!isClient()) {
            throw new NexusException("Client-only action attempted on server: " + action);
        }
    }

    public static void expectDedicatedServer(String action) {
        if (!isDedicatedServer()) {
            throw new NexusException("Dedicated-server-only action attempted on client: " + action);
        }
    }

    public static void safeClientOnly(Runnable runnable) {
        runOnClient(runnable);
    }

    private static boolean boolProperty(String key) {
        return Boolean.parseBoolean(System.getProperty(key, "false"));
    }

    private static boolean boolEnv(String key) {
        return Boolean.parseBoolean(System.getenv().getOrDefault(key, "false"));
    }

    private NexusEnvironment() {
    }
}
