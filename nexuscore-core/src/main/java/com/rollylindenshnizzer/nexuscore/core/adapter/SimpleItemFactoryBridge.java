package com.rollylindenshnizzer.nexuscore.core.adapter;

import com.rollylindenshnizzer.nexuscore.api.item.NexusItemDefinition;
import com.rollylindenshnizzer.nexuscore.bridge.registry.ItemFactoryBridge;

public final class SimpleItemFactoryBridge implements ItemFactoryBridge {
    @Override
    public Object createItem(NexusItemDefinition definition) {
        return definition;
    }
}
