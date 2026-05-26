package com.rollylindenshnizzer.nexuscore.neoforge;

import com.rollylindenshnizzer.nexuscore.NexusCore;
import com.rollylindenshnizzer.nexuscore.data.NexusData;
import com.rollylindenshnizzer.nexuscore.data.NexusDataProvider;
import com.rollylindenshnizzer.nexuscore.neoforge.test.NexusCoreNeoForgeGameTests;
import dev.architectury.platform.Platform;
import dev.architectury.utils.Env;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.fml.common.Mod;

/**
 * NeoForge loader entrypoint.
 *
 * <p>NeoForge constructs this class because it is annotated with @Mod. Register
 * NeoForge DeferredRegisters and mod-event-bus listeners here, then call common init.</p>
 */
@Mod(NexusCore.MOD_ID)
public final class NexusCoreNeoForge {
    public NexusCoreNeoForge(IEventBus modBus) {
        NexusCore.init();
        modBus.addListener(this::gatherData);
        modBus.addListener(this::registerGameTests);
        if (Platform.getEnvironment() == Env.CLIENT) {
            initClientByReflection();
        }
    }

    private void gatherData(GatherDataEvent event) {
        event.addProvider(new NexusDataProvider(event.getGenerator().getPackOutput(), NexusCore.MOD_ID, NexusData.plan(NexusCore.MOD_ID)));
    }

    private void registerGameTests(RegisterGameTestsEvent event) {
        event.register(NexusCoreNeoForgeGameTests.class);
    }

    private static void initClientByReflection() {
        try {
            Class.forName("com.rollylindenshnizzer.nexuscore.client.NexusCoreClient")
                    .getMethod("init")
                    .invoke(null);
        } catch (ReflectiveOperationException exception) {
            NexusCore.LOGGER.error("Failed to initialize NexusCore client runtime", exception);
        }
    }
}
