package com.rollylindenshnizzer.nexuscore.adapter.quilt.mc1201;

import com.rollylindenshnizzer.nexuscore.adapter.common.NexusTargetEntrypoint;
import com.rollylindenshnizzer.nexuscore.core.service.ServiceProvider;

public final class NexusCoreQuilt1201Entrypoint extends NexusTargetEntrypoint {
    @Override
    protected ServiceProvider createServiceProvider() {
        return new Quilt1201ServiceProvider();
    }
}
