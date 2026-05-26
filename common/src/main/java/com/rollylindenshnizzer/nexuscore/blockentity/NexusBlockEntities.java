package com.rollylindenshnizzer.nexuscore.blockentity;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

public final class NexusBlockEntities {
    public static <T extends BlockEntity> BlockEntityTypeBuilder<T> blockEntity(String modId, String path,
                                                                                 BlockEntityType.BlockEntitySupplier<? extends T> factory,
                                                                                 Block... blocks) {
        return BlockEntityTypeBuilder.of(modId, path, factory, blocks);
    }

    public static void setChangedAndSync(BlockEntity blockEntity) {
        blockEntity.setChanged();
        if (blockEntity.getLevel() != null) {
            blockEntity.getLevel().sendBlockUpdated(blockEntity.getBlockPos(), blockEntity.getBlockState(), blockEntity.getBlockState(), 3);
        }
    }

    private NexusBlockEntities() {
    }
}
