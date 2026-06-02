package com.rollylindenshnizzer.nexuscore.testmod.target.neoforge.mc1211;

import com.rollylindenshnizzer.nexuscore.adapter.neoforge.mc1211.NeoForge1211ServiceProvider;
import com.rollylindenshnizzer.nexuscore.core.bootstrap.NexusCoreCommonBootstrap;
import com.rollylindenshnizzer.nexuscore.core.service.NexusServices;
import com.rollylindenshnizzer.nexuscore.testmod.NexusCoreTestMod;

public final class NexusCoreTestModNeoForge1211 {
    public void onInitialize() {
        NexusServices.install(new NeoForge1211ServiceProvider());
        NexusCoreTestMod.init();
        NexusCoreCommonBootstrap.init();
    }
}
