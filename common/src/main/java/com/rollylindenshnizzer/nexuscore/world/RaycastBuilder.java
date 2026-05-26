package com.rollylindenshnizzer.nexuscore.world;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

public final class RaycastBuilder {
    private final Level level;
    private Vec3 from;
    private Vec3 to;
    private ClipContext.Block blockMode = ClipContext.Block.OUTLINE;
    private ClipContext.Fluid fluidMode = ClipContext.Fluid.NONE;
    private Entity source;

    public RaycastBuilder(Level level, Vec3 from, Vec3 to) {
        this.level = level;
        this.from = from;
        this.to = to;
    }

    public RaycastBuilder blockMode(ClipContext.Block blockMode) {
        this.blockMode = blockMode;
        return this;
    }

    public RaycastBuilder fluidMode(ClipContext.Fluid fluidMode) {
        this.fluidMode = fluidMode;
        return this;
    }

    public RaycastBuilder source(Entity source) {
        this.source = source;
        return this;
    }

    public BlockHitResult cast() {
        return level.clip(new ClipContext(from, to, blockMode, fluidMode, source));
    }
}
