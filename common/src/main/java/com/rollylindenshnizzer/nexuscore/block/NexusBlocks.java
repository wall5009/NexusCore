package com.rollylindenshnizzer.nexuscore.block;

import com.rollylindenshnizzer.nexuscore.NexusCore;
import net.minecraft.util.ColorRGBA;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ColoredFallingBlock;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

public final class NexusBlocks {
    public static BlockBuilder<Block> block(String path) {
        return block(NexusCore.MOD_ID, path);
    }

    public static BlockBuilder<Block> block(String modId, String path) {
        return new BlockBuilder<>(modId, path);
    }

    public static BlockBuilder<RotatedPillarBlock> pillar(String modId, String path) {
        return new BlockBuilder<RotatedPillarBlock>(modId, path).factory(RotatedPillarBlock::new);
    }

    public static BlockBuilder<SlabBlock> slab(String modId, String path) {
        return new BlockBuilder<SlabBlock>(modId, path).factory(SlabBlock::new);
    }

    public static BlockBuilder<StairBlock> stairs(String modId, String path, Block base) {
        return new BlockBuilder<StairBlock>(modId, path).factory(properties -> new StairBlock(base.defaultBlockState(), properties));
    }

    public static BlockBuilder<FallingBlock> falling(String modId, String path) {
        return new BlockBuilder<FallingBlock>(modId, path).factory(properties -> new ColoredFallingBlock(new ColorRGBA(0xFFFFFFFF), properties));
    }

    public static BlockBehaviour.Properties stoneLike(float hardness, float resistance) {
        return BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(hardness, resistance).requiresCorrectToolForDrops().sound(SoundType.STONE);
    }

    private NexusBlocks() {
    }
}
