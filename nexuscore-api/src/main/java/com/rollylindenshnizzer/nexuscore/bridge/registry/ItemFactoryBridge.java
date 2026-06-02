package com.rollylindenshnizzer.nexuscore.bridge.registry;

import com.rollylindenshnizzer.nexuscore.api.item.NexusItemDefinition;

public interface ItemFactoryBridge {
    Object createItem(NexusItemDefinition definition);
}
