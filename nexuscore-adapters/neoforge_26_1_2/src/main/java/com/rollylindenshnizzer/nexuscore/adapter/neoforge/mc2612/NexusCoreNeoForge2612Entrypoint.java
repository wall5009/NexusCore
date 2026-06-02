package com.rollylindenshnizzer.nexuscore.adapter.neoforge.mc2612;

import com.rollylindenshnizzer.nexuscore.adapter.common.NexusTargetEntrypoint;
import com.rollylindenshnizzer.nexuscore.core.service.ServiceProvider;

public final class NexusCoreNeoForge2612Entrypoint extends NexusTargetEntrypoint {
    @Override
    protected ServiceProvider createServiceProvider() {
        return new NeoForge2612ServiceProvider();
    }
}
