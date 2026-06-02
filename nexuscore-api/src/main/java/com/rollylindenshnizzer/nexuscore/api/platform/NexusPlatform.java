package com.rollylindenshnizzer.nexuscore.api.platform;

import com.rollylindenshnizzer.nexuscore.core.service.NexusServices;

public final class NexusPlatform {
    public static NexusTarget target() {
        return NexusServices.get().target();
    }

    public static String targetId() {
        return target().targetId();
    }

    public static NexusLoader loader() {
        return target().loader();
    }

    public static String minecraftVersion() {
        return target().minecraftVersion();
    }

    public static String mappings() {
        return target().mappings();
    }

    public static NexusRuntimeSide side() {
        return target().side();
    }

    public static boolean isClient() {
        return side() == NexusRuntimeSide.CLIENT;
    }

    public static boolean isServer() {
        return side() == NexusRuntimeSide.SERVER;
    }

    public static boolean isFabric() {
        return loader() == NexusLoader.FABRIC;
    }

    public static boolean isForge() {
        return loader() == NexusLoader.FORGE;
    }

    public static boolean isNeoForge() {
        return loader() == NexusLoader.NEOFORGE;
    }

    public static boolean isQuilt() {
        return loader() == NexusLoader.QUILT;
    }

    public static boolean supports(NexusFeature feature) {
        return NexusServices.get().capabilities().supports(feature);
    }
}
