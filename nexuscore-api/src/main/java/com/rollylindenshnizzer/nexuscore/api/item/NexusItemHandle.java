package com.rollylindenshnizzer.nexuscore.api.item;

public final class NexusItemHandle {
    private final NexusItemDefinition definition;
    private volatile Object nativeItem;

    public NexusItemHandle(NexusItemDefinition definition, Object nativeItem) {
        this.definition = definition;
        this.nativeItem = nativeItem;
    }

    public NexusItemDefinition definition() {
        return definition;
    }

    public Object nativeItem() {
        return nativeItem;
    }

    public void attachNativeItem(Object nativeItem) {
        this.nativeItem = nativeItem;
    }
}
