package com.rollylindenshnizzer.nexuscore.example.neoforge;

import com.rollylindenshnizzer.nexuscore.NexusCore;
import com.rollylindenshnizzer.nexuscore.data.NexusDataProvider;
import com.rollylindenshnizzer.nexuscore.example.NexusCoreExampleContent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.fml.common.Mod;

@Mod(NexusCoreExampleContent.MOD_ID)
public final class NexusCoreExampleNeoForge {
    public NexusCoreExampleNeoForge(IEventBus modBus) {
        NexusCore.init();
        NexusCoreExampleContent.bootstrap();
        modBus.addListener(this::gatherData);
    }

    private void gatherData(GatherDataEvent event) {
        event.addProvider(new NexusDataProvider(event.getGenerator().getPackOutput(),
                NexusCoreExampleContent.MOD_ID, NexusCoreExampleContent.populateGeneratedData()));
    }
}
