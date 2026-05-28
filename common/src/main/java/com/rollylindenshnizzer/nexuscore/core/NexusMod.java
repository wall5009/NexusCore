package com.rollylindenshnizzer.nexuscore.core;

import com.rollylindenshnizzer.nexuscore.registry.ContentModule;
import com.rollylindenshnizzer.nexuscore.registry.ContentModuleManager;
import com.rollylindenshnizzer.nexuscore.registry.NexusRegistries;
import com.rollylindenshnizzer.nexuscore.runtime.NexusRuntimeContent;
import org.slf4j.Logger;

import java.util.Collection;
import java.util.List;

/**
 * Base class for mods that want a small, consistent bootstrap sequence.
 */
public abstract class NexusMod {
    private final String modId;
    private final Logger logger;
    private boolean initialized;

    protected NexusMod(String modId) {
        this.modId = NexusIds.requireNamespace(modId);
        this.logger = NexusLoggers.get(modId);
    }

    public final String modId() {
        return modId;
    }

    public final Logger logger() {
        return logger;
    }

    public final void init() {
        if (initialized) {
            return;
        }
        initialized = true;
        NexusLifecycle.installArchitecturyHooks();
        NexusLifecycle.fire(NexusLifecycle.Phase.PRE_INIT);
        beforeRegistries();
        ContentModuleManager modules = ContentModuleManager.create(modId, modules());
        modules.initialize();
        NexusRuntimeContent.install(modId);
        NexusRegistries.group(modId).registerAll();
        onInitialize();
        NexusLifecycle.fire(NexusLifecycle.Phase.COMMON_INIT);
        NexusLifecycle.fire(NexusLifecycle.Phase.POST_REGISTRY_VALIDATION);
        NexusDiagnostics.startup(modId).log(logger);
    }

    protected void beforeRegistries() {
    }

    protected abstract void onInitialize();

    protected Collection<? extends ContentModule> modules() {
        return List.of();
    }
}
