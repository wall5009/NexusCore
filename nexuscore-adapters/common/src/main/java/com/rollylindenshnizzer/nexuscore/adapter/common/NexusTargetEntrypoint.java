package com.rollylindenshnizzer.nexuscore.adapter.common;

import com.rollylindenshnizzer.nexuscore.api.datagen.NexusDataGen;
import com.rollylindenshnizzer.nexuscore.core.bootstrap.NexusCoreCommonBootstrap;
import com.rollylindenshnizzer.nexuscore.core.service.NexusServices;
import com.rollylindenshnizzer.nexuscore.core.service.ServiceProvider;

import java.nio.file.Path;
import java.util.List;

public abstract class NexusTargetEntrypoint {
    protected abstract ServiceProvider createServiceProvider();

    public final void onInitialize() {
        ensureInstalled();
        NexusCoreCommonBootstrap.init();
    }

    public final void onInitializeClient(Object client) {
        ensureInstalled();
        NexusServices.get().events().fireClientStarted(client);
    }

    public final void onInitializeServer(Object server) {
        ensureInstalled();
        NexusCoreCommonBootstrap.init();
        NexusServices.get().events().fireServerStarted(server);
    }

    public final List<Path> runDataGen(NexusDataGen dataGen) {
        ensureInstalled();
        return NexusServices.get().datagen().generate(dataGen);
    }

    private void ensureInstalled() {
        if (!NexusServices.isInstalled()) {
            NexusServices.install(createServiceProvider());
        }
    }
}
