package com.rollylindenshnizzer.nexuscore.testmod;

import com.rollylindenshnizzer.nexuscore.api.block.NexusBlockHandle;
import com.rollylindenshnizzer.nexuscore.api.block.NexusBlocks;
import com.rollylindenshnizzer.nexuscore.api.item.NexusItemHandle;
import com.rollylindenshnizzer.nexuscore.api.item.NexusItems;
import com.rollylindenshnizzer.nexuscore.api.registry.NexusCreativeTabs;
import com.rollylindenshnizzer.nexuscore.api.registry.NexusEntry;

public final class TestContent {
    public static NexusEntry<NexusItemHandle> TEST_GEM;
    public static NexusEntry<NexusBlockHandle> TEST_BLOCK;

    private TestContent() {
    }

    public static void register() {
        TEST_GEM = NexusItems.create(NexusCoreTestMod.MOD, "test_gem")
            .creativeTab("ingredients")
            .register();

        TEST_BLOCK = NexusBlocks.create(NexusCoreTestMod.MOD, "test_block")
            .strength(3.0f)
            .requiresTool()
            .withSimpleItem()
            .creativeTab("building_blocks")
            .register();

        NexusCreativeTabs.create(NexusCoreTestMod.MOD, "test_tab")
            .title("NexusCore Test Mod")
            .icon(TEST_GEM.id())
            .entry(TEST_GEM.id())
            .entry(TEST_BLOCK.id())
            .register();
    }
}
