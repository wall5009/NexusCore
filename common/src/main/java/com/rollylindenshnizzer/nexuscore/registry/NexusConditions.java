package com.rollylindenshnizzer.nexuscore.registry;

import com.rollylindenshnizzer.nexuscore.core.NexusEnvironment;

import java.util.function.BooleanSupplier;

public final class NexusConditions {
    public static BooleanSupplier always() {
        return () -> true;
    }

    public static BooleanSupplier modLoaded(String modId) {
        return () -> NexusEnvironment.isModLoaded(modId);
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

    private NexusConditions() {
    }
}
