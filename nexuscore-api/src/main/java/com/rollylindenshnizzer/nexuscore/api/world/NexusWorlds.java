package com.rollylindenshnizzer.nexuscore.api.world;

import com.rollylindenshnizzer.nexuscore.api.platform.NexusFeature;
import com.rollylindenshnizzer.nexuscore.api.platform.NexusPlatform;

public final class NexusWorlds {
    private NexusWorlds() {
    }

    public static boolean worldgenAvailable() {
        return NexusPlatform.supports(NexusFeature.WORLDGEN);
    }
}
