package com.rollylindenshnizzer.nexuscore.world;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.stream.Stream;

public final class NexusWorlds {
    public static Optional<ServerLevel> level(MinecraftServer server, ResourceKey<Level> key) {
        return Optional.ofNullable(server.getLevel(key));
    }

    public static Stream<BlockPos> positions(BlockPos first, BlockPos second) {
        return BlockPos.betweenClosedStream(first, second).map(BlockPos::immutable);
    }

    public static Stream<BlockPos> radius(BlockPos center, int radius) {
        return BlockPos.betweenClosedStream(center.offset(-radius, -radius, -radius), center.offset(radius, radius, radius)).map(BlockPos::immutable);
    }

    public static <T extends Entity> List<T> nearbyEntities(Level level, Class<T> type, BlockPos center, double radius, Predicate<T> predicate) {
        AABB box = AABB.ofSize(center.getCenter(), radius * 2, radius * 2, radius * 2);
        return level.getEntitiesOfClass(type, box, predicate);
    }

    public static Optional<Entity> entity(ServerLevel level, UUID uuid) {
        return Optional.ofNullable(level.getEntity(uuid));
    }

    private NexusWorlds() {
    }
}
