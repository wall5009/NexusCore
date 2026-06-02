package com.rollylindenshnizzer.nexuscore.core.adapter;

import com.rollylindenshnizzer.nexuscore.api.block.NexusBlockDefinition;
import com.rollylindenshnizzer.nexuscore.api.item.NexusItemDefinition;
import com.rollylindenshnizzer.nexuscore.bridge.registry.BlockFactoryBridge;

import java.util.Map;

public final class SimpleBlockFactoryBridge implements BlockFactoryBridge {
    @Override
    public Object createBlock(NexusBlockDefinition definition) {
        return definition;
    }

    @Override
    public Object createBlockItem(NexusBlockDefinition definition, Object block) {
        return new NexusItemDefinition(definition.modId(), definition.id(), definition.creativeTab(), 64, false, 0, 0.0f, Map.of("block", definition.fullId()));
    }
}
