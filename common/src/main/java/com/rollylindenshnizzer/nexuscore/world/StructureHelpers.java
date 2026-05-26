package com.rollylindenshnizzer.nexuscore.world;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.AABB;

public final class StructureHelpers {
    public static AABB box(BoundingBox box) {
        return AABB.of(box);
    }

    public static BlockPos center(BoundingBox box) {
        return new BlockPos((box.minX() + box.maxX()) / 2, (box.minY() + box.maxY()) / 2, (box.minZ() + box.maxZ()) / 2);
    }

    private StructureHelpers() {
    }
}
