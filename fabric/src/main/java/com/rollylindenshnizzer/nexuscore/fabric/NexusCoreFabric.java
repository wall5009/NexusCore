package com.rollylindenshnizzer.nexuscore.fabric;

import com.rollylindenshnizzer.nexuscore.NexusCore;
import com.rollylindenshnizzer.nexuscore.core.NexusLifecycle;
import com.rollylindenshnizzer.nexuscore.fabric.worldgen.FabricWorldgenRegistrar;
import net.fabricmc.api.ModInitializer;

/**
 * Fabric loader entrypoint.
 *
 * <p>Fabric calls this class because it is listed in fabric.mod.json. Put
 * Fabric-only setup here. Shared setup should stay in NexusCore.init().</p>
 */
public final class NexusCoreFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        NexusCore.init();
        NexusLifecycle.on(NexusLifecycle.Phase.COMMON_SETUP, FabricWorldgenRegistrar::install);
    }
}
