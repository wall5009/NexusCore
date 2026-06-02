package com.rollylindenshnizzer.nexuscore.adapter.fabric.mc1201;

import com.rollylindenshnizzer.nexuscore.adapter.common.NexusTargetEntrypoint;
import com.rollylindenshnizzer.nexuscore.core.service.ServiceProvider;

public final class NexusCoreFabric1201Entrypoint extends NexusTargetEntrypoint {
    @Override
    protected ServiceProvider createServiceProvider() {
        return new Fabric1201ServiceProvider();
    }
}
