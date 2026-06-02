package com.rollylindenshnizzer.nexuscore.api.block;

public final class NexusBlockHandle {
    private final NexusBlockDefinition definition;
    private volatile Object nativeBlock;
    private volatile Object nativeBlockItem;

    public NexusBlockHandle(NexusBlockDefinition definition, Object nativeBlock, Object nativeBlockItem) {
        this.definition = definition;
        this.nativeBlock = nativeBlock;
        this.nativeBlockItem = nativeBlockItem;
    }

    public NexusBlockDefinition definition() {
        return definition;
    }

    public Object nativeBlock() {
        return nativeBlock;
    }

    public Object nativeBlockItem() {
        return nativeBlockItem;
    }

    public void attachNativeBlock(Object nativeBlock) {
        this.nativeBlock = nativeBlock;
    }

    public void attachNativeBlockItem(Object nativeBlockItem) {
        this.nativeBlockItem = nativeBlockItem;
    }
}
