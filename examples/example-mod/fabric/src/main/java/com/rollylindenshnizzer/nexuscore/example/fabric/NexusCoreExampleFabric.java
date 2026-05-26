package com.rollylindenshnizzer.nexuscore.example.fabric;

import com.rollylindenshnizzer.nexuscore.NexusCore;
import com.rollylindenshnizzer.nexuscore.example.NexusCoreExampleContent;
import net.fabricmc.api.ModInitializer;

public final class NexusCoreExampleFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        NexusCore.init();
        NexusCoreExampleContent.bootstrap();
    }
}
