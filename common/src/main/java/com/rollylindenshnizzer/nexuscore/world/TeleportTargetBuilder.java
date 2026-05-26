package com.rollylindenshnizzer.nexuscore.world;

import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

public final class TeleportTargetBuilder {
    private final ResourceKey<Level> dimension;
    private Vec3 position = Vec3.ZERO;
    private Vec2 rotation = Vec2.ZERO;

    public TeleportTargetBuilder(ResourceKey<Level> dimension) {
        this.dimension = dimension;
    }

    public TeleportTargetBuilder position(Vec3 position) {
        this.position = position;
        return this;
    }

    public TeleportTargetBuilder rotation(Vec2 rotation) {
        this.rotation = rotation;
        return this;
    }

    public Optional<Target> resolve(MinecraftServer server) {
        return Optional.ofNullable(server.getLevel(dimension)).map(level -> new Target(level, position, rotation));
    }

    public record Target(ServerLevel level, Vec3 position, Vec2 rotation) {
    }
}
