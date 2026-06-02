package com.rollylindenshnizzer.nexuscore.adapter.quilt.mc1211;

import com.rollylindenshnizzer.nexuscore.adapter.common.NexusTargetEntrypoint;
import com.rollylindenshnizzer.nexuscore.core.service.ServiceProvider;

public final class NexusCoreQuilt1211Entrypoint extends NexusTargetEntrypoint {
    @Override
    protected ServiceProvider createServiceProvider() {
        return new Quilt1211ServiceProvider();
    }
}
