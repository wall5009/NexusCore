package com.rollylindenshnizzer.nexuscore.block;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Collection;

public final class NexusShapes {
    public static VoxelShape pixelBox(double x1, double y1, double z1, double x2, double y2, double z2) {
        return Block.box(x1, y1, z1, x2, y2, z2);
    }

    public static VoxelShape cube() {
        return Shapes.block();
    }

    public static VoxelShape slab() {
        return pixelBox(0, 0, 0, 16, 8, 16);
    }

    public static VoxelShape post(double width) {
        double inset = (16.0 - width) / 2.0;
        return pixelBox(inset, 0, inset, 16 - inset, 16, 16 - inset);
    }

    public static VoxelShape combine(Collection<VoxelShape> shapes) {
        VoxelShape result = Shapes.empty();
        for (VoxelShape shape : shapes) {
            result = Shapes.or(result, shape);
        }
        return result.optimize();
    }

    public static VoxelShape rotateHorizontal(VoxelShape shape, Direction direction) {
        return switch (direction) {
            case SOUTH -> rotateY(shape, 2);
            case WEST -> rotateY(shape, 3);
            case EAST -> rotateY(shape, 1);
            default -> shape;
        };
    }

    private static VoxelShape rotateY(VoxelShape shape, int quarters) {
        VoxelShape result = shape;
        for (int i = 0; i < quarters; i++) {
            VoxelShape rotated = Shapes.empty();
            for (var box : result.toAabbs()) {
                rotated = Shapes.or(rotated, Shapes.create(1 - box.maxZ, box.minY, box.minX, 1 - box.minZ, box.maxY, box.maxX));
            }
            result = rotated.optimize();
        }
        return result;
    }

    private NexusShapes() {
    }
}
