package com.rollylindenshnizzer.nexuscore.inventory;

import java.util.List;

public final class InventoryLayouts {
    public static List<SlotRange> singleSlot() {
        return List.of(new SlotRange(0, 1));
    }

    public static List<SlotRange> inputOutput() {
        return List.of(new SlotRange(0, 1), new SlotRange(1, 2));
    }

    public static List<SlotRange> threeByThree() {
        return List.of(new SlotRange(0, 9));
    }

    public static List<SlotRange> playerInventoryWithHotbar(int start) {
        return List.of(new SlotRange(start, start + 27), new SlotRange(start + 27, start + 36));
    }

    private InventoryLayouts() {
    }
}
