package com.rollylindenshnizzer.nexuscore.dimension;

import com.rollylindenshnizzer.nexuscore.api.NexusStable;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;

@NexusStable(since = "1.3")
public final class PortalRuntimeBlock extends Block {
    private final NexusDimensions.PortalDefinition portal;

    public PortalRuntimeBlock(NexusDimensions.PortalDefinition portal, BlockBehaviour.Properties properties) {
        super(properties);
        this.portal = portal;
    }

    public NexusDimensions.PortalDefinition portal() {
        return portal;
    }
}
