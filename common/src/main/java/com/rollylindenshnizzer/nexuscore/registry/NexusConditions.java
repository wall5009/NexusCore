package com.rollylindenshnizzer.nexuscore.registry;

import com.rollylindenshnizzer.nexuscore.core.NexusEnvironment;

import java.util.Locale;
import java.util.function.BooleanSupplier;

public final class NexusConditions {
    public static BooleanSupplier always() {
        return () -> true;
    }

    public static BooleanSupplier modLoaded(String modId) {
        return () -> NexusEnvironment.isModLoaded(modId);
    }

    public static BooleanSupplier modMissing(String modId) {
        return () -> !NexusEnvironment.isModLoaded(modId);
    }

    public static BooleanSupplier loader(String loaderName) {
        String expected = loaderName.toLowerCase(Locale.ROOT);
        return () -> NexusEnvironment.loader().equalsIgnoreCase(expected);
    }

    public static BooleanSupplier clientOnly() {
        return NexusEnvironment::isClient;
    }

    public static BooleanSupplier serverOnly() {
        return NexusEnvironment::isDedicatedServer;
    }

    public static BooleanSupplier devOnly() {
        return NexusEnvironment::isDevelopment;
    }

    public static BooleanSupplier datagenOnly() {
        return () -> Boolean.getBoolean("fabric-api.datagen") || Boolean.getBoolean("nexus.datagen");
    }

    public static BooleanSupplier testOnly() {
        return () -> Boolean.getBoolean("fabric-api.gametest") || Boolean.getBoolean("nexus.gametest");
    }

    public static BooleanSupplier configValue(BooleanSupplier supplier) {
        return supplier;
    }

    public static BooleanSupplier minecraftVersionRange(String ignoredRange) {
        return () -> true;
    }

    public static BooleanSupplier all(BooleanSupplier... suppliers) {
        return () -> {
            for (BooleanSupplier supplier : suppliers) {
                if (!supplier.getAsBoolean()) {
                    return false;
                }
            }
            return true;
        };
    }

    public static BooleanSupplier any(BooleanSupplier... suppliers) {
        return () -> {
            for (BooleanSupplier supplier : suppliers) {
                if (supplier.getAsBoolean()) {
                    return true;
                }
            }
            return false;
        };
    }

    public static ConditionReport explain(String name, BooleanSupplier supplier) {
        boolean passed = supplier.getAsBoolean();
        return new ConditionReport(name, passed, passed ? "condition passed" : "condition failed");
    }

    public record ConditionReport(String name, boolean passed, String explanation) {
    }

    private NexusConditions() {
    }
}
