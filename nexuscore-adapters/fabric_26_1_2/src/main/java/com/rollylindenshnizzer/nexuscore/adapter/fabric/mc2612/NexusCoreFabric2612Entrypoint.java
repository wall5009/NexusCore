package com.rollylindenshnizzer.nexuscore.adapter.fabric.mc2612;

import com.rollylindenshnizzer.nexuscore.adapter.common.NexusTargetEntrypoint;
import com.rollylindenshnizzer.nexuscore.core.service.ServiceProvider;

public final class NexusCoreFabric2612Entrypoint extends NexusTargetEntrypoint {
    @Override
    protected ServiceProvider createServiceProvider() {
        return new Fabric2612ServiceProvider();
    }
}
