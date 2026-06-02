package com.rollylindenshnizzer.nexuscore.testmod.target.neoforge.mc2612;

import com.rollylindenshnizzer.nexuscore.adapter.neoforge.mc2612.NeoForge2612ServiceProvider;
import com.rollylindenshnizzer.nexuscore.core.bootstrap.NexusCoreCommonBootstrap;
import com.rollylindenshnizzer.nexuscore.core.service.NexusServices;
import com.rollylindenshnizzer.nexuscore.testmod.NexusCoreTestMod;

public final class NexusCoreTestModNeoForge2612 {
    public void onInitialize() {
        NexusServices.install(new NeoForge2612ServiceProvider());
        NexusCoreTestMod.init();
        NexusCoreCommonBootstrap.init();
    }
}
