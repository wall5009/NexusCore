package com.rollylindenshnizzer.nexuscore.fabric;

import com.rollylindenshnizzer.nexuscore.client.NexusCoreClient;
import net.fabricmc.api.ClientModInitializer;

public final class NexusCoreFabricClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        NexusCoreClient.init();
    }
}
