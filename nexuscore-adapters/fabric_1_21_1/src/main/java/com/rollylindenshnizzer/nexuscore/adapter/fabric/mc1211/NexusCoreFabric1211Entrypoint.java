package com.rollylindenshnizzer.nexuscore.adapter.fabric.mc1211;

import com.rollylindenshnizzer.nexuscore.adapter.common.NexusTargetEntrypoint;
import com.rollylindenshnizzer.nexuscore.core.service.ServiceProvider;

public final class NexusCoreFabric1211Entrypoint extends NexusTargetEntrypoint {
    @Override
    protected ServiceProvider createServiceProvider() {
        return new Fabric1211ServiceProvider();
    }
}
