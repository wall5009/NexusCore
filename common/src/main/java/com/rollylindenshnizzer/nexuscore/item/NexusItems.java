package com.rollylindenshnizzer.nexuscore.item;

import com.rollylindenshnizzer.nexuscore.NexusCore;
import com.rollylindenshnizzer.nexuscore.registry.CreativeTabBuilder;

public final class NexusItems {
    public static ItemBuilder<NexusItem> item(String path) {
        return item(NexusCore.MOD_ID, path);
    }

    public static ItemBuilder<NexusItem> item(String modId, String path) {
        return new ItemBuilder<>(modId, path);
    }

    public static FoodBuilder food() {
        return new FoodBuilder();
    }

    public static CreativeTabBuilder creativeTab(String modId, String path) {
        return new CreativeTabBuilder(modId, path);
    }

    private NexusItems() {
    }
}
