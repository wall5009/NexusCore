package com.rollylindenshnizzer.nexuscore.testmod;

import com.rollylindenshnizzer.nexuscore.api.event.NexusEvents;

public final class TestEvents {
    private static boolean commonSetupFired;
    private static boolean clientStartedFired;
    private static boolean serverStartedFired;
    private static Object nativeClient;
    private static Object nativeServer;

    private TestEvents() {
    }

    public static void register() {
        NexusEvents.COMMON_SETUP.register(() -> {
            commonSetupFired = true;
            NexusCoreTestMod.MOD.logger().info("Common setup event fired.");
        });
        NexusEvents.COMMON_SETUP.register(TestAssertions::runCommonSetupAssertions);
        NexusEvents.CLIENT_STARTED.register(client -> {
            clientStartedFired = true;
            nativeClient = client;
            NexusCoreTestMod.MOD.logger().info("Client started event fired.");
            TestAssertions.runClientAssertions(client);
        });
        NexusEvents.SERVER_STARTED.register(server -> {
            serverStartedFired = true;
            nativeServer = server;
            NexusCoreTestMod.MOD.logger().info("Server started event fired.");
            TestAssertions.runServerAssertions(server);
        });
        NexusEvents.PLAYER_JOINED.register(player -> {
            player.sendMessage("Welcome to the NexusCore test mod.");
            TestNetwork.sendJoinPacket(player);
        });
        NexusEvents.SERVER_TICK.register(tick -> {
            if (tick == 1L) {
                NexusCoreTestMod.MOD.logger().debug("First server tick observed.");
            }
        });
    }

    static boolean commonSetupFired() {
        return commonSetupFired;
    }

    static boolean clientStartedFired() {
        return clientStartedFired;
    }

    static Object nativeClient() {
        return nativeClient;
    }

    static boolean serverStartedFired() {
        return serverStartedFired;
    }

    static Object nativeServer() {
        return nativeServer;
    }
}
