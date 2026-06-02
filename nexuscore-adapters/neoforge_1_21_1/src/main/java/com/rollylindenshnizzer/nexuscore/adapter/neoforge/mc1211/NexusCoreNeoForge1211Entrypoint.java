package com.rollylindenshnizzer.nexuscore.adapter.neoforge.mc1211;

import com.rollylindenshnizzer.nexuscore.adapter.common.NexusTargetEntrypoint;
import com.rollylindenshnizzer.nexuscore.core.service.ServiceProvider;

public final class NexusCoreNeoForge1211Entrypoint extends NexusTargetEntrypoint {
    @Override
    protected ServiceProvider createServiceProvider() {
        return new NeoForge1211ServiceProvider();
    }
}
