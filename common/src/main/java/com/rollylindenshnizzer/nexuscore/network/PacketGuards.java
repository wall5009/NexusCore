package com.rollylindenshnizzer.nexuscore.network;

import com.rollylindenshnizzer.nexuscore.core.PacketValidationException;
import dev.architectury.networking.NetworkManager;
import dev.architectury.utils.Env;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public final class PacketGuards {
    public static void requireServerSide(ResourceLocation packetId, NetworkManager.PacketContext context) {
        if (context.getEnvironment() != Env.SERVER) {
            throw new PacketValidationException(packetId, "expected server-side handling");
        }
    }

    public static ServerPlayer requireServerPlayer(ResourceLocation packetId, NetworkManager.PacketContext context) {
        if (context.getPlayer() instanceof ServerPlayer serverPlayer) {
            return serverPlayer;
        }
        throw new PacketValidationException(packetId, "expected a server player");
    }

    public static void runOnMainThread(NetworkManager.PacketContext context, Runnable task) {
        context.queue(task);
    }

    private PacketGuards() {
    }
}
