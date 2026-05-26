package com.rollylindenshnizzer.nexuscore.blockentity;

import com.rollylindenshnizzer.nexuscore.core.NexusIds;
import com.rollylindenshnizzer.nexuscore.registry.NexusRegistries;
import dev.architectury.registry.registries.RegistrySupplier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

public final class BlockEntityTypeBuilder<T extends BlockEntity> {
    private final String modId;
    private final String path;
    private final BlockEntityType.BlockEntitySupplier<? extends T> factory;
    private final Block[] blocks;

    private BlockEntityTypeBuilder(String modId, String path, BlockEntityType.BlockEntitySupplier<? extends T> factory, Block... blocks) {
        this.modId = NexusIds.requireNamespace(modId);
        this.path = NexusIds.normalizePath(path);
        this.factory = factory;
        this.blocks = blocks;
    }

    public static <T extends BlockEntity> BlockEntityTypeBuilder<T> of(String modId, String path,
                                                                        BlockEntityType.BlockEntitySupplier<? extends T> factory,
                                                                        Block... blocks) {
        return new BlockEntityTypeBuilder<>(modId, path, factory, blocks);
    }

    @SuppressWarnings("unchecked")
    public RegistrySupplier<BlockEntityType<T>> register() {
        return (RegistrySupplier<BlockEntityType<T>>) (RegistrySupplier<?>) NexusRegistries.group(modId).blockEntities()
                .register(path, () -> BlockEntityType.Builder.of(factory, blocks).build(null));
    }
}
