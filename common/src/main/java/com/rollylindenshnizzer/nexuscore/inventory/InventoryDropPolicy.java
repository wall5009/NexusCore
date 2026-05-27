package com.rollylindenshnizzer.nexuscore.inventory;

import com.rollylindenshnizzer.nexuscore.api.NexusStable;

@NexusStable(since = "1.2")
public enum InventoryDropPolicy {
    DROP_ALL,
    KEEP_ALL,
    DROP_OUTPUTS_ONLY,
    DROP_NON_GHOST,
    MIGRATE_TO_COMPONENTS
}
