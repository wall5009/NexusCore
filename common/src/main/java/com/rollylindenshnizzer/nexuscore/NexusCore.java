package com.rollylindenshnizzer.nexuscore;

import com.rollylindenshnizzer.nexuscore.core.NexusDiagnostics;
import com.rollylindenshnizzer.nexuscore.core.NexusLifecycle;
import com.rollylindenshnizzer.nexuscore.command.NexusCoreCommands;
import com.mojang.logging.LogUtils;
import com.rollylindenshnizzer.nexuscore.debug.NexusDebugWorkbench;
import com.rollylindenshnizzer.nexuscore.registry.NexusRegistries;
import com.rollylindenshnizzer.nexuscore.runtime.NexusRuntimeContent;
import com.rollylindenshnizzer.nexuscore.world.NexusWorldEventHooks;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;

/**
 * Shared entrypoint for code that is safe to run on every selected loader.
 *
 * <p>The Fabric, Quilt-compatible Fabric, Forge, and NeoForge modules each call
 * {@link #init()} from their own loader entrypoint. Keep generic setup here and
 * keep loader-only APIs inside the matching loader module.</p>
 */
public final class NexusCore {
    /** The namespace used by registries, assets, data files, packets, and logs. */
    public static final String MOD_ID = "nexuscore";

    /** Shared logger. Keep this logger available from generated example classes. */
    public static final Logger LOGGER = LogUtils.getLogger();

    private static boolean initialized = false;

    /**
     * Initializes common systems once.
     *
     * <p>The guard is useful because multiple loader modules can call this method
     * during development. Optional generated examples may insert calls here.</p>
     */
    public static void init() {
        if (initialized) {
            return;
        }
        initialized = true;
        NexusLifecycle.installArchitecturyHooks();
        NexusDebugWorkbench.installV13Tabs();
        NexusCoreCommands.install();
        NexusWorldEventHooks.install();
        NexusLifecycle.fire(NexusLifecycle.Phase.PRE_INIT);
        NexusRuntimeContent.install(MOD_ID);
        NexusRegistries.registerAll(MOD_ID);
        NexusLifecycle.fire(NexusLifecycle.Phase.COMMON_INIT);
        NexusDiagnostics.startup(MOD_ID).log(LOGGER);
        LOGGER.info("Initialized {}", MOD_ID);
    }

    /**
     * Creates a namespaced identifier for this mod.
     *
     * <p>Use this instead of repeating the mod ID everywhere. In 1.21.1,
     * ResourceLocation uses the static factory shown below.</p>
     */
    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }

    private NexusCore() {
    }
}
