package com.rollylindenshnizzer.nexuscore.security;

import com.rollylindenshnizzer.nexuscore.core.NexusException;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.level.Level;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class ServerAuthority {
    private static final Map<UUID, Long> LAST_ACTION = new ConcurrentHashMap<>();

    public static void requireServer(Level level, String action) {
        if (level.isClientSide()) {
            throw new NexusException("Client attempted server-authoritative action: " + action);
        }
    }

    public static boolean antiSpam(ServerPlayer player, String action, long cooldownMillis) {
        UUID key = UUID.nameUUIDFromBytes((player.getUUID() + ":" + action).getBytes(java.nio.charset.StandardCharsets.UTF_8));
        long now = System.currentTimeMillis();
        long previous = LAST_ACTION.getOrDefault(key, 0L);
        if (now - previous < cooldownMillis) {
            return false;
        }
        LAST_ACTION.put(key, now);
        return true;
    }

    public static boolean withinDistance(ServerPlayer player, BlockPos pos, double maxDistance) {
        return player.blockPosition().distSqr(pos) <= maxDistance * maxDistance;
    }

    public static boolean sameDimension(ServerPlayer player, Level level) {
        return player.level().dimension().equals(level.dimension());
    }

    public static boolean canOpenMenu(ServerPlayer player, MenuProvider provider, BlockPos pos, double maxDistance) {
        return provider != null && withinDistance(player, pos, maxDistance);
    }

    private ServerAuthority() {
    }
}
