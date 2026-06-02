package com.rollylindenshnizzer.nexuscore.bridge.registry;

import com.rollylindenshnizzer.nexuscore.api.block.NexusBlockDefinition;

public interface BlockFactoryBridge {
    Object createBlock(NexusBlockDefinition definition);

    Object createBlockItem(NexusBlockDefinition definition, Object block);
}
