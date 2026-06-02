package com.rollylindenshnizzer.nexuscore.testmod.target.forge.mc1201;

import com.rollylindenshnizzer.nexuscore.adapter.forge.mc1201.Forge1201ServiceProvider;
import com.rollylindenshnizzer.nexuscore.core.bootstrap.NexusCoreCommonBootstrap;
import com.rollylindenshnizzer.nexuscore.core.service.NexusServices;
import com.rollylindenshnizzer.nexuscore.testmod.NexusCoreTestMod;

public final class NexusCoreTestModForge1201 {
    public void onInitialize() {
        NexusServices.install(new Forge1201ServiceProvider());
        NexusCoreTestMod.init();
        NexusCoreCommonBootstrap.init();
    }
}
