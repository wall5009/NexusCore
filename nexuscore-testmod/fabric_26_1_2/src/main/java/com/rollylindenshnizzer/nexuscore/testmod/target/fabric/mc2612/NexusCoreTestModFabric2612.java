package com.rollylindenshnizzer.nexuscore.testmod.target.fabric.mc2612;

import com.rollylindenshnizzer.nexuscore.adapter.fabric.mc2612.Fabric2612ServiceProvider;
import com.rollylindenshnizzer.nexuscore.core.bootstrap.NexusCoreCommonBootstrap;
import com.rollylindenshnizzer.nexuscore.core.service.NexusServices;
import com.rollylindenshnizzer.nexuscore.testmod.NexusCoreTestMod;

public final class NexusCoreTestModFabric2612 {
    public void onInitialize() {
        NexusServices.install(new Fabric2612ServiceProvider());
        NexusCoreTestMod.init();
        NexusCoreCommonBootstrap.init();
    }
}
