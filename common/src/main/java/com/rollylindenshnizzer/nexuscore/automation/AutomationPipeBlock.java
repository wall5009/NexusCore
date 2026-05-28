package com.rollylindenshnizzer.nexuscore.automation;

import com.rollylindenshnizzer.nexuscore.api.NexusStable;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;

@NexusStable(since = "1.3")
public final class AutomationPipeBlock extends Block {
    private final NexusAutomation.AutomationBlockPreset preset;

    public AutomationPipeBlock(NexusAutomation.AutomationBlockPreset preset, BlockBehaviour.Properties properties) {
        super(properties);
        this.preset = preset;
    }

    public NexusAutomation.AutomationBlockPreset preset() {
        return preset;
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
        return true;
    }
}
