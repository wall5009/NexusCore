package com.rollylindenshnizzer.nexuscore.inventory;

import com.rollylindenshnizzer.nexuscore.api.NexusStable;

@NexusStable(since = "1.2")
public enum SlotRole {
    INPUT,
    OUTPUT,
    CATALYST,
    UPGRADE,
    FILTER,
    GHOST,
    PLAYER_MAIN,
    PLAYER_HOTBAR
}
